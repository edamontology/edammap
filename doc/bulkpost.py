#!/usr/bin/python

# Usage: python bulkpost.py list.json

TOKEN = "09633b86b4be69e5633e45a0bbcbbb1c7d79ceaa"
SITE = "bio.tools"

import httplib
import json
import sys

tools = None

try:
    script, json_file = sys.argv
    with open(json_file) as data_file:
        tools = json.load(data_file)
except IOError:
    err_type, err_value, err_trace = sys.exc_info()
    print(str(err_type) + str(err_value))
    exit()
except:
    print("Usage: python bulkpost.py list.json")
    exit()

headers = {"Authorization": "Token " + TOKEN,
           "Content-Type": "application/json"}

report = ""

def postTool(tool):
    con = httplib.HTTPSConnection(SITE, 443, timeout=10)
    con.request("POST", "/api/tool/", json.dumps(tool), headers)
    res = con.getresponse()
    return json.load(res)

def success(id, details=""):
    return id + " was added successfully.\n\n\n\n\n"

def fail(id, details="empty"):
    return id + " adding failed.\n Details: " + details + "\n\n\n\n\n"


err_tools = []
for tool in tools:
    try:
        data = postTool(tool)
        if data.get("id", False):
            if type(data["id"]) == str:
                report += success(data["id"])
            else:
                report += fail(tool["id"], json.dumps(data))
                err_tools.append(tool)
        else:
            report += fail(tool["id"], json.dumps(data))
            err_tools.append(tool)
    except:
        err_type, err_value, err_trace = sys.exc_info()
        report += fail(tool["id"], str(err_type) + str(err_value))
        err_tools.append(tool)

with open("log.txt", "w+") as log_file:
    log_file.write(report)
    log_file.close()

with open("error_tools.json", "w+") as err_tools_file:
    err_tools_file.write(json.dumps(err_tools, sort_keys=True, indent=4, separators=(',', ': ')))
    err_tools_file.close()
