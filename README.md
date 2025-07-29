<!-- Improved compatibility of back to top link: See: https://github.com/othneildrew/Best-README-Template/pull/73 -->
<a id="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![project_license][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/BounceU/BenReader">
    <img src="JavaSide/benreader/icons/app_icon.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">BenReader</h3>

  <p align="center">
	A local Text-to-speech AI audiobook generator, converting EPUB files into audiobooks. Designed to work with a <a href="https://github.com/BounceU/BenReaderSwiftUI">companion iOS app</a>, but can be used to generate standard audio files as well. 
    <br />
  </p>
</div>




<!-- ABOUT THE PROJECT -->
## About The Project

BenReader is a Java app and Python CLI that uses the [Kokoro-82m](https://huggingface.co/hexgrad/Kokoro-82M) TTS model.  

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- GETTING STARTED -->
## Getting Started


### Prerequisites

* Java JDK 18+: [Oracle](https://www.oracle.com/java/technologies/downloads/), [Microsoft](https://learn.microsoft.com/en-us/java/openjdk/download)
* [Maven](https://maven.apache.org/download.cgi)
* [FFMPEG](https://ffmpeg.org/download.html)
* [Miniconda](https://www.anaconda.com/docs/getting-started/miniconda/install)

### Building the project locally

1. Clone the repo
   ```sh
   git clone https://github.com/BounceU/BenReader
   ```
2. Build the maven project (in JavaSide/benreader/):
   ```sh
   mvn clean compile assembly:single
   ```
3. Set up your conda environment (in PythonSide/):
   ```sh
   conda env create -f environment.yaml
   conda activate book_ai
   python -m spacy download en_core_web_sm
   ```
4. Build the python project using pyinstaller (in PythonSide/):
   ```sh
   pyinstaller --onedir tts_epub.py --add-data "YOUR_MINICONDA_INSTALLATION/envs/book_ai/Lib/site-packages;." --noconfirm
   ```
5. Setup file structure
   ```sh
	BenReader/
	├─ benreader.jar
	├─ icons/
	│  ├─ add_book.png
	│  ├─ app_icon.ico
	│  ├─ app_icon.png
	│  ├─ clean.png
	│  ├─ default_cover.png
	│  ├─ export_book.png
	│  ├─ generate_audio.png
	│  ├─ remove_book.png
	│  ├─ settings.png
	├─ res/
	├─ tts/
	│  ├─ _internal/
	│  ├─ tts_epub (executable)
   ```
   The jar is the one you compiled in step 2, and the contents of the `tts` folder are the contents of the `dist/tts_epub/` folder you compiled in step 4. With this, the program is done and you just need to run the jar. Internal program files will appear when you start using the program, like a `config.json` file.
6. (Optional) Build as application with jpackage
   
   In the `BenReader` directory you created, run the following to create an installer
   - Windows:
		```sh
		jpackage --input "." --name "BenReader" --main-jar "benreader.jar" --icon "icons/app_icon.ico" --win-per-user-install --type msi --win-shortcut --win-menu
		```
	- MacOOS:
		```sh
		jpackage --input "." --name "BenReader" --main-jar "benreader.jar" --mac-sign --type dmg
		```

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

Ben Liebkemann - ben@liebkemann.com

Project Link: [https://github.com/BounceU/BenReader](https://github.com/BounceU/BenReader)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* [othneildrew's README template](https://github.com/othneildrew/Best-README-Template)
<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/BounceU/BenReader.svg?style=flat
[contributors-url]: https://github.com/BounceU/BenReader/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/BounceU/BenReader.svg?style=flat
[forks-url]: https://github.com/BounceU/BenReader/network/members
[stars-shield]: https://img.shields.io/github/stars/BounceU/BenReader.svg?style=flat
[stars-url]: https://github.com/BounceU/BenReader/stargazers
[issues-shield]: https://img.shields.io/github/issues/BounceU/BenReader.svg?style=flat
[issues-url]: https://github.com/BounceU/BenReader/issues
[license-shield]: https://img.shields.io/github/license/BounceU/BenReader.svg?style=flat
[license-url]: https://github.com/BounceU/BenReader/blob/main/LICENSE
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=flat&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/ben-liebkemann