#!/usr/bin/env python
from urllib.request import urlopen
from bs4 import BeautifulSoup
import codecs
import re
import json
from collections import OrderedDict
import sys
import argparse


def removing_extra(EDAM_url):
    EDAM_url_long = "http://bioportal.bioontology.org/ontologies/EDAM?p=classes&conceptid=http%3A%2F%2Fedamontology.org%2F" + EDAM_url.split("/")[-1]
    f2 = urlopen(EDAM_url_long)
    html2 = f2.read()
    soup2 = BeautifulSoup(html2, "lxml")
    annotation = soup2.find_all("span", {"class" : "prefLabel"})[0]
    return annotation.text

    
def finding_EDAM_annotations(level, EDAM_ont_term):
    r = []
    annotations = level.find_all("tr", {"class" : EDAM_ont_term})
    for el in annotations:
        annotations_list = el.find_all("td", {"class" : "match"})
        EDAM_concept_match_type = el.find_all("td", {"class" : "type"})
        EDAM_url = el.find("a")["href"]
        if EDAM_concept_match_type[0].text != "label":
            try:
                annotation = removing_extra(EDAM_url)
                r.append([annotation, EDAM_url])
            except:
                pass
        else:
            annotation = annotations_list[0].text
            r.append([annotation, EDAM_url])
    return r

def top_and_op(op_top_list, format_type):
    l = []
    for el in op_top_list:
        r1 = OrderedDict()
        r1["term"] = el[0]
        r1["uri"] = el[1]
        l.append(r1)
    return {format_type: l}

def parsing_html(args):
    
    tool_type = args.t
    
    html_doc = args.l

    output_file = args.o
    
    f=codecs.open(html_doc, "r", "utf-8")
    soup = BeautifulSoup("".join(f), "lxml")

    number_of_tools = int(soup.find_all("dd")[-2].text)

    dict_tools = {}

    for i in range(number_of_tools):
        level = soup("tbody")[i]
        tool_id = level["id"]
        tool_name = level.h3.contents[1]
        print(tool_name)
       
        tool_description = level.p.string
        
        # annotations (EDAM ontology terms: topic, operation)
        # topic
        topic = finding_EDAM_annotations(level, "row topic")
        
        # operation
        operation = finding_EDAM_annotations(level, "row operation")
        
        dict_tools[tool_name] = [tool_description, topic, operation]

    # json
    with open(output_file, "w") as f:
        f.write("[")
        for tool in dict_tools:
            tool_name = tool
            description = dict_tools[tool][0]
            topic_format = top_and_op(dict_tools[tool][1], "topic")
            operation_format = top_and_op(dict_tools[tool][2], "operation")

            entire_json_tuple = [("toolType", [tool_type]),
                   ("name", tool_name), ("id", tool_name.replace(" ", "_")), ("description", description)]
            
            entire_json = OrderedDict(entire_json_tuple)
            
            entire_json.update({"topic": topic_format["topic"]})
            entire_json.move_to_end("topic", last = False)

            entire_json.update({"function": [{**operation_format}]})
            entire_json.move_to_end("function", last = False)
              
            json.dump(entire_json, f)
            f.write(",")
        f.write("]")

parser = argparse.ArgumentParser(description= "Insert tool type")
parser.add_argument('-l', required=True)
parser.add_argument('-t', default="Web application")
parser.add_argument('-o', required=True)

if __name__ == '__main__':
    args = parser.parse_args()
    parsing_html(args)




