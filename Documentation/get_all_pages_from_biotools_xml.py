#!/usr/bin/env python
import xml.etree.ElementTree as ET
from urllib.request import urlopen
import math

def all_pages():
    tree = ET.ElementTree(file=urlopen("https://bio.tools/api/tool/?format=xml"))
    root = tree.getroot()
    number_of_tools = root.find("count").text
    number_of_pages = math.ceil(int(number_of_tools) / 25)


    for i in range(1,number_of_pages +1):
        url = "https://bio.tools/api/tool/?page=" + str(i) + "&format=xml"
        s = urlopen(url)
        contents = s.read()
        file = open("page" + str(i) + ".xml", 'w', encoding = "UTF-8")
        file.write(contents.decode())
        file.close()

if __name__ == '__main__':
    all_pages()
