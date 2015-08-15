#!/usr/bin/python

from subprocess import call, Popen, PIPE, STDOUT
import re

def exportKeycodes(layoutName, layoutDescription, variantName=None, variantDescription=None):
    print('set keyboard layout {layoutName=' + str(layoutName or '') + ', layoutDescription=' + str(layoutDescription or '') + ', variantName=' + str(variantName or '') + ', variantDescription=' + str(variantDescription or '') + '}')

    #change keyboard layout
    returnCode = call(['setxkbmap', '-layout', str(layoutName or ''), '-variant', str(variantName or '')])
    if (returnCode is not 0):
        print('couldn\'t change keyboard layout')
        return False

    #export keycode mapping
    xkbExporterProcess = Popen(['./xkb-exporter', '--list-keycodes'], stdout=PIPE, stderr=PIPE)

    filepath = 'keycodes/' + layoutName
    if (variantName is not None):
        filepath = filepath + '-' + variantName

    try:
        with open(filepath, 'w') as filePointer:
            filePointer.write('#layoutName=' + str(layoutName or '') + '\n')
            filePointer.write('#layoutDescription=' + str(layoutDescription or '') + '\n')
            filePointer.write('#variantName=' + str(variantName or '') + '\n')
            filePointer.write('#variantDescription=' + str(variantDescription or '') + '\n')
            for line in xkbExporterProcess.stdout:
                filePointer.write(line)
    except IOError:
        print('couldn\'t write to file \'' + filepath + '\'')
        return False

    return True

xkbExporterProcess = Popen(['./xkb-exporter', '--list-layouts'], stdout=PIPE, stderr=PIPE)
delemiterRegex = re.compile(r'(?<!\\),')
for line in xkbExporterProcess.stdout:
    line = line.split('#')[0].strip()
    cols = delemiterRegex.split(line.strip())
    cols = [col.replace('\\,', ',') for col in cols]
    if len(cols) >= 2:
        layoutName = cols[0]
        layoutDescription = cols[1]

        exportKeycodes(layoutName, layoutDescription)

        variants = cols[2:]
        variants = zip(variants[0::2], variants[1::2])
        for variant in variants:
            variantName = variant[0]
            variantDescription = variant[1]

            exportKeycodes(layoutName, layoutDescription, variantName, variantDescription)
            



