@echo off
cd "./JavaSide/benreader/"
echo "Compiling java program"
call mvn clean compile assembly:single > NUL
echo "Done."
cd "../"
cd "../"
echo "Moving program to output folder"
xcopy "./JavaSide/benreader/icons" "./BenReaderCompiled/icons" /I /E /Y > NUL
cd "./JavaSide/benreader/target"
copy "benreader-1.0-jar-with-dependencies.jar" "../../../BenReaderCompiled/benreader.jar" /Y > NUL
cd "../"
call mvn clean > NUL
cd "../../"

if not exist "./BenReaderCompiled/tts/tts_epub.exe" (
echo "Compiling python program"
cd "./PythonSide/"
call conda activate book_ai > NUL
call pyinstaller --onefile tts_epub.py --add-data "C:/Users/ben/miniconda3/envs/book_ai/Lib/site-packages;." --noconfirm 2> NUL
call conda activate base > NUL
echo "Done."
cd "../"
echo "Moving python program to output folder"
xcopy "./PythonSide/dist/tts_epub" "./BenReaderCompiled/tts" /I /E /Y > NUL
call conda deactivate > NUL
)
if exist "./PythonSide/dist" rmdir "./PythonSide/dist" /S /Q > NUL
if exist "./PythonSide/build" rmdir "./PythonSide/build" /S /Q > NUL
cd "./BenReaderCompiled"

if not exist "./res" mkdir "./res"
echo "Done."