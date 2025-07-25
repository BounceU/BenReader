
import soundfile as sf
from tqdm import tqdm
from html.parser import HTMLParser
import ebooklib
from ebooklib import epub
import subprocess
import sys
import os
from datetime import timedelta
import torch
import re
import shutil
import copy
import argparse
import platform
from kokoro import KPipeline
import json


parser = argparse.ArgumentParser(description="A script to generate an AI audiobook from an epub file")
parser.add_argument("-v", "--voice", type=str, required=True, help = "The voice(s) to use. Separate by commas to combine multiple voices into one. ex: bm_george,bm_george,bm_george,bm_george,bm_george,bm_lewis,bm_lewis,bm_lewis")
parser.add_argument("-i", "--input", type=str, required=True, help = "Required. The file to use as input.")
parser.add_argument("-d", "--directory", type=str, required=True, help = "Required. The directory to use as a working directory.")
parser.add_argument("-a", "--audio", type=str, default="m4a", help = "The type of audio file to use. m4a is more accurate but larger. mp3 is smaller but less accurate")
parser.add_argument("-m", "--machine", action="store_true", help="Flag denoting that this is being used as a backend for a GUI, changes behavior, do not use if running through the command line")

args = parser.parse_args()

if(args.audio != "m4a" and args.audio != "mp3"):
	print("Error! audio must be either m4a or mp3")
	exit(1)

def fileNamitize(inString):
	return "".join(x for x in inString if x.isalnum())

print(f'Cuda: {torch.cuda.is_available()}')
print(f'MPS: {torch.backends.mps.is_available()}')

fileIn = args.input
workingDirectory = os.path.dirname(args.directory + os.sep)

# Get the book name by cutting off the `.epub` from the filename
bookName = os.path.basename(fileIn).replace(".epub", "")

useBookName = fileNamitize(bookName)

if(args.machine):
	print(f'Config:\nVoice:\t{args.voice}\nInput file:\t{args.input}\nWorking Directory:\t{args.directory}\nAudio Type:\t{args.audio}', flush=True)

# Load the book
book = epub.read_epub(fileIn)


# Function to handle removal of all non-apostrophe single-quotes
def replace_char_if_not_surrounded_by_letters(text, char_to_replace, replacement_char):
	char_list = list(text)
	for i, char in enumerate(char_list):
		if char == char_to_replace:
			if (i > 0 and i < len(char_list) - 1 and
					char_list[i - 1].isalpha() and char_list[i + 1].isalpha()):
				continue 
			else:
				char_list[i] = replacement_char
	return "".join(char_list)

# Class to parse the html files
class MyHTMLParser(HTMLParser):
	# Setup
	content = ""
	prevData = ""
	texts = [[]]
	
	# Split the input string by a delimeter. Do something special for periods to allow for elipses.
	def getSplitString(self, data, delimiter):
		if(delimiter == "."):
			newData = re.split(r"(?<!\.)\.(?!\.)", data)
		else:
			newData = data.split(delimiter)
		if(len(newData) > 1):
			for i in range(0, len(newData) - 1):
				newData[i] = newData[i] + delimiter
			if(newData[-1] == ""):
				newData.pop()
			if(len(newData) > 1):
				if(len(newData[-1]) == 1):
					newString=  newData.pop()
					newData[-1] = newData[-1] + newString
		return newData


	# This occurs when we call `.feed()`
	def handle_data(self, data):

		# Do nothing if empty
		if(data.strip() == ""):
			return
		
		# We're doing the same thing no matter what
		if(len(self.prevData) == 1):
			newData = self.getSplitString(data, ".")
			newData2 = []
			for dat in newData:
				newData2.extend(self.getSplitString(dat, "?"))
			self.texts[-1].extend(newData2)
			self.content = self.content +  data
		else:
			newData = self.getSplitString(data, ".")
			newData2 = []
			for dat in newData:
				newData2.extend(self.getSplitString(dat, "?"))
			self.texts[-1].extend(newData2)
			self.content = self.content + "\n" + data
		self.prevData = data

# Set up structure to read the text content of the book. Each chapter is a list of strings corresponding to different text in tags
# and `texts` is a list of chapters
texts = []
labels = []
textIndices = []
allItems = []
skipTo = -1


book_enumeration = enumerate(book.get_items()) if args.machine else tqdm(enumerate(book.get_items()), desc="Loading book")

# Go through each item in the book
for index, item in book_enumeration:
	allItems.append(item)

	# If it's a chapter
	if item.get_type() == ebooklib.ITEM_DOCUMENT:
		if(item.get_name() == "nav.xhtml"):
			continue
		# Skip table of contents
		# if(index == 1):
		# 	continue
		# Get html parser
		f = MyHTMLParser()
		# Get body content (text) of item 
		bodyContent = item.get_body_content().decode()
		# Parse it
		f.feed(bodyContent)
		# Put the parsed content into texts
		texts.append(copy.deepcopy(f.texts[-1]))
		if(len(texts[-1]) == 0):
			labels.append("<empty>")
		else:
			title_label = copy.deepcopy(texts[-1][0])
			title_label = "".join(title_label.splitlines()).strip()
			labels.append(title_label)
		textIndices.append(index)

		# Allow for more data in the parser
		f.texts.append([])
		# I had to do it this way because the parser seemed to be read only



def listChapters(texts, labels):
	for i, label in enumerate(labels):
		print(f'{i}: {label}', flush=True)

def redoList(texts, labels, textIndices, allItems, skip):
	print("Chapters:", flush=True)
	listChapters(texts, labels)


	getThrough = False
	removables = []

	while not getThrough:
		a = input()
		if(a[0:2] == "s "):
			b = a.removeprefix("s ")
			try:
				skip = int(b)
			except ValueError as e:
				skip
		if(a[0:2] == "r "):
			b = a.removeprefix("r ")
			try:
				removables.append(int(b))
			except ValueError as e:
				pass
		if(a[0:2] == "a "):
			b = a.removeprefix("a ")
			if(b == "mp3" or b == "m4a"):
				args.audio = b
		if(a[0:1] == "g"):
			getThrough = True

	removables = sorted(list(set(removables)), reverse=True)

	for i in removables:
		del labels[i]
		del texts[i]
		del allItems[textIndices[i]]
	
	for i, item in reversed(list(enumerate(allItems))):
		if item.get_name() == "nav.xhtml" or item.get_name() == "toc.ncx":
			del allItems[i]

	return texts, labels, textIndices, allItems, skip

if(not args.machine):
	print("Use \"r #\" to delete a chapter, and then \"g\" to start generation", flush=True)

texts, labels, textIndices, allItems, skipTo = redoList(texts, labels, textIndices, allItems, skipTo)


# Create new book
new_book = epub.EpubBook()
new_book.FOLDER_NAME = "OEBPS"
new_book.set_identifier(book.get_metadata('DC', 'identifier')[0][0])
new_book.set_language(book.get_metadata('DC','language')[0][0])
new_book.set_title(book.get_metadata('DC', "title")[0][0])
for author in book.get_metadata('DC', 'creator'):
	new_book.add_author(author[0])

for item in allItems:
		new_book.add_item(item)

new_toc = []
for link in book.toc:
	if isinstance(link, epub.Link):
		if any(chapter.file_name == link.href for chapter in allItems):
			new_toc.append(link)
	elif isinstance(link, tuple):
		section_title, section_links = link
		filtered_section_links = [chap for chap in section_links if chap in allItems]
		if filtered_section_links:
			new_toc.append((section_title, tuple(filtered_section_links)))
new_book.toc = tuple(new_toc)

new_book.add_item(epub.EpubNcx())
new_book.add_item(epub.EpubNav())

new_spine = []
for items in book.spine:
	item = items[0]
	if isinstance(item, str) and item != 'nav':
		item_obj = book.get_item_with_id(item)
		if item_obj in allItems:
			new_spine.append(item)
	elif item in allItems:
		new_spine.append(item)
new_book.spine = new_spine
epub.write_epub(f'{workingDirectory}{os.path.sep}{useBookName}.epub', new_book)





# Load pipeline, might generate some warnings but should be okay
if(args.voice.startswith('b')):
	pipeline = KPipeline(lang_code='b')
else:
	pipeline = KPipeline(lang_code='a')



# Set up variables to track timestamps and audio length
timeStamps = ""
totalSecs = 0
totalLength = 0

fileNames = ""

if(args.machine):
	print("Progress:", flush=True)

text_enumeration = enumerate(texts) if args.machine else tqdm(enumerate(texts), total=len(texts), desc="Generating chapters")

for index, text in text_enumeration: # enumerate(texts): #
	
	if(index < skipTo):
		fileNames = fileNames + f'file \'{index}.wav\'\n'
		continue
	if(index == skipTo):
		fileNames = fileNames + f'file \'{index}.wav\'\n'
		if(os.path.exists(f'{workingDirectory}{os.path.sep}tmp2.txt')):
			with open(f'{workingDirectory}{os.path.sep}tmp2.txt', 'r') as file:
				timeStamps = file.read()
				file.close()
			with open(f'{workingDirectory}{os.path.sep}tmp1.txt', 'r') as file:
				inString = file.read()
				things = inString.splitlines()
				if(len(things) < 2):
					totalSecs = 0
					totalLength = 0
				else:
					totalLength = float(things[0])
					totalSecs = float(things[1])
				file.close()
		continue
			

	# Set up the structure to hold the .wav data
	totalAudio = torch.Tensor([])
	
	# Log the timestamp of the start of this audio file / chapter
	timeStamps = timeStamps + "c " + str(timedelta(seconds=totalSecs)) + "\n"
	
	# Select voice pack
	voice_pack = args.voice
	# if args.voice == "ben":
	# 	voice_pack = 'bm_george,bm_george,bm_george,bm_george,bm_george,bm_lewis,bm_lewis,bm_lewis'
	# if args.voice == "dad":
	# 	voice_pack = 'am_adam,am_adam,am_adam,am_adam,am_adam,am_adam,am_adam,am_adam,am_adam,am_adam,am_adam,bm_george,bm_george,bm_george,bm_george,bm_lewis,bm_lewis,bm_lewis,bm_lewis,bm_lewis,bm_lewis'
	
	# Create new text, removing all single-elements
	newText = [item for item in text if item.strip() != "." and item.strip() != "?" and item.strip() != "!" and item.strip() != "," and re.search(r'(?![a-zA-Z0-9?.]+)', item)]
	useText = "\n".join(newText)
	
	# These can mess up audio
	#for old_char in ["'", "\"", "«", "»", "‘", "’", "‚", "‛", "“", "”", "„", "‟", "‹", "›", "❛", "❜", "❝", "❞", "〝", "〞", "〟", "＂", "＇", "′", "″", "‴", "⁗", "‵", "‶", "‷"]:
	for old_char in ["\"", "«", "»", "‚","“", "”", "„", "‟", "‹", "›", "❛", "❝", "❞", "〝", "〞", "〟", "＂", "″", "‴", "⁗", "‶", "‷", "]", "[", "[", "]", "~"]:
		useText = useText.replace(old_char, "")
	# These can also mess up audio, UNLESS they are an apostrophe. Check with regex
	useText = replace_char_if_not_surrounded_by_letters(useText, "'", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "’", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "′", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "‘", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "‛", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "❜", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "‵", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, ".", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "＇", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "-", "")
	useText = replace_char_if_not_surrounded_by_letters(useText, "–", "")

	anotherText = useText.splitlines()

	newText =  [item for item in anotherText if item.strip() != "." and item.strip() != "?" and item.strip() != "!" and item.strip() != "," and re.search(r'(?![a-zA-Z0-9?.]+)', item)]
	useText = "\n".join(newText)

	# Create generator for the text
	generator = pipeline(useText, voice=voice_pack, split_pattern=r'\n+')#
	miniLength = 0

	audio_enumeration = enumerate(generator) if args.machine else tqdm(enumerate(generator), desc="Generating audio clip")

	# Generate each audio clip
	for i, (gs, ps, audio) in audio_enumeration: # enumerate(generator): #

		# Log the timestamp of the start of this audio clip
		timeStamps = timeStamps + "p " + str(timedelta(seconds=totalSecs)) + "\n"

		# Keep track of the total time
		totalSecs += audio.numpy().shape[0] / 24000.

		# Combine all audio clips into one
		totalAudio = torch.cat((totalAudio, audio))
	
	# Write the total audio clip into a .wav file
	sf.write(f'{workingDirectory}{os.path.sep}{index}.wav', totalAudio, 24000)
	fileNames = fileNames + f'file \'{index}.wav\'\n'
	
	# Length of audio file in seconds
	miniLength = totalAudio.numpy().shape[0] / 24000.

	# Length of all generated audio files
	totalLength += miniLength

	with open(f'{workingDirectory}{os.path.sep}tmp1.txt', 'w') as file:
		file.write(f'{totalLength}\n{totalSecs}')
		file.close()
	with open(f'{workingDirectory}{os.path.sep}tmp2.txt', 'w') as file:
		file.write(timeStamps)
		file.close()

	if(args.machine):
		print(index, flush=True)

print("done", flush=True)

# Run script to combine all the chapter audio files into a complete book. Basically creates a list of all the .wav files, 
# sorts them, then combines them into one ALAC (.m4a) lossless audio file using FFMPEG

print(f'combining into {args.audio}', flush=True)
with open(f'{workingDirectory}{os.path.sep}chapterList.txt', "w") as file:
	file.write(fileNames)
	file.close()

if(args.audio == "mp3"):
	subprocess.run(["ffmpeg", "-f", "concat", "-i", "chapterList.txt", "-acodec", "libmp3lame", "-q:a", "4", useBookName + ".mp3"], cwd = workingDirectory, capture_output=True)
else:
	subprocess.run(["ffmpeg", "-f", "concat", "-i", "chapterList.txt", "-acodec", "alac", useBookName + ".m4a"], cwd = workingDirectory, capture_output=True)
	
print("done", flush=True)

print("Creating file configuration", flush=True)
file_data = {
	"audio_file": f'{useBookName}.{args.audio}',
	"book_file": f'{useBookName}.epub',
	"timing_file": f'{useBookName}.txt',
	"audio_type": f'{args.audio}'
}

with open(f'{workingDirectory}{os.path.sep}metadata.json', 'w') as file:
	json.dump(file_data, file, indent=4)
	file.close()

print("done", flush=True)


print("zipping", flush=True)

# Write file with timestamps
with open(f'{workingDirectory}{os.path.sep}{useBookName}.txt', "w") as file:
	file.write(timeStamps)
	file.close()

if(platform.system() == 'Windows'):
	subprocess.run("del chapterList.txt", cwd=workingDirectory, shell=True)
	subprocess.run("del tmp1.txt", cwd=workingDirectory, shell=True)
	subprocess.run("del tmp2.txt", cwd=workingDirectory, shell=True)
	subprocess.run("del *.wav", cwd=workingDirectory, shell=True)
	subprocess.run(f'tar -a -c -f {useBookName}.zip {useBookName}.{args.audio} {useBookName}.epub {useBookName}.txt metadata.json', cwd=workingDirectory)
	subprocess.run(f'del {useBookName}.{args.audio}', cwd=workingDirectory, shell=True)
	subprocess.run(f'del {useBookName}.txt', cwd=workingDirectory, shell=True)
	subprocess.run(f'del {useBookName}.epub', cwd=workingDirectory, shell=True)
	subprocess.run(f'del metadata.json', cwd=workingDirectory, shell=True)
elif(platform.system() == 'Darwin' or platform.system() == 'Linux'):
	subprocess.run(["rm", "chapterList.txt"], cwd=workingDirectory)
	subprocess.run(["rm", "tmp1.txt"], cwd=workingDirectory)
	subprocess.run(["rm", "tmp2.txt"], cwd=workingDirectory)
	subprocess.run("rm -f *.wav", cwd=workingDirectory, shell=True)
	subprocess.run(["zip", "-X", useBookName + ".zip", useBookName + "." + args.audio, useBookName + ".epub", useBookName + ".txt", "metadata.json"], cwd=workingDirectory)
	subprocess.run(f'rm -f {useBookName}.{args.audio}', cwd=workingDirectory, shell=True)
	subprocess.run(f'rm -f {useBookName}.txt', cwd=workingDirectory, shell=True)
	subprocess.run(f'rm -f {useBookName}.epub', cwd=workingDirectory, shell=True)
	subprocess.run(f'rm -f metadata.json', cwd=workingDirectory, shell=True)


print("done", flush=True)


# Print length of final output audio file
print("Total Length: " + str(timedelta(seconds=totalLength)))



## BOTH:
#  ffmpeg -f concat -i mylist.txt -acodec alac "%arg1%.m4a"

## MAC/LINUX:
# rm chapterList.txt
# rm *.wav

## WINDOWS:
# del chapterList.txt
# del *.wav


