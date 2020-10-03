from bs4 import BeautifulSoup 
from datetime import datetime, timedelta
import requests
import re
import time
import subprocess


def sendmessage(header,message):
    subprocess.Popen(['notify-send', header,message])
    return


def get_upcoming_contests(url):
	soup =  BeautifulSoup(requests.get(url).text, "lxml")
	tables = soup.findAll("table")
	upcoming_table = tables[0]
	rows = upcoming_table.findAll("tr")
	concerned = []
	for row in rows[1:]:
		try:
			contest_name = row.findAll("td")[0].find(text = True)
			temp = datetime.strptime(row.findAll("td")[4].find("span").find(text = True), '%H:%M:%S')
			before_start = timedelta(hours = temp.hour, minutes = temp.minute, seconds = temp.second)
			concerned.append({"name": contest_name, "time left": before_start})
		except:
			continue
	return concerned


def notify_these(contests):
	fifteen_m = timedelta(minutes = 15)
	for x in contests:
		if fifteen_m >= x['time left']:
			header = 'Codeforces reminder!' 
			message = x['name'] + "\nStarts in " + str(x['time left'])
			sendmessage(header, message)


if __name__ == "__main__":
	url = "http://codeforces.com/contests"
	while True:
		try:
			notify_these(get_upcoming_contests(url))
			time.sleep(5*60)
		except:
			time.sleep(5*60)
