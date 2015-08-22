#!/usr/bin/python

from subprocess import call, Popen, PIPE, STDOUT
import xml.etree.ElementTree as ET

def exportKeycodes(layoutName, layoutDescription, variantName=None, variantDescription=None):
    print('set keyboard layout {layoutName=' + (layoutName or '') + ', layoutDescription=' + (layoutDescription or '') + ', variantName=' + (variantName or '') + ', variantDescription=' + (variantDescription or '') + '}')

    #change keyboard layout
    returnCode = call(['setxkbmap', '-layout', str(layoutName or ''), '-variant', str(variantName or '')])
    if (returnCode is not 0):
        print('couldn\'t change keyboard layout')
        return False

    #export keycode mapping
    xkbExporterProcess = Popen(['./xkb-exporter', '--list-keycodes'], stdout=PIPE, stderr=PIPE)
    data = xkbExporterProcess.stdout.read()
    xkbExporterProcess.wait()
    returnCode = xkbExporterProcess.returncode

    if (returnCode is not 0):
        print('couldn\'t export keycodes')
        return False

    filepath = 'keycodes/' + layoutName
    if (variantName is not None):
        filepath = filepath + '-' + variantName

    try:
        with open(filepath, 'w') as filePointer:
            filePointer.write(data)
    except IOError:
        print('couldn\'t write to file \'' + filepath + '\'')
        return False



    return True

xkbExporterProcess = Popen(['./xkb-exporter', '--list-layouts'], stdout=PIPE, stderr=PIPE)
root = ET.fromstring(xkbExporterProcess.stdout.read())

for layout in root.findall('layout'):
    layoutName = layout.find('name').text
    layoutDescription = layout.find('description').text

    exportKeycodes(layoutName, layoutDescription)
    variants = layout.find('variants')
    if (variants is not None):
        for variant in variants.findall('variant'):
            variantName = variant.find('name').text
            variantDescription = variant.find('description').text
            
            exportKeycodes(layoutName, layoutDescription, variantName, variantDescription)
