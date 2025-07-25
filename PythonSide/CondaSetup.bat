call conda deactivate
echo y | call conda env remove -n book_ai
call conda env create -f environment.yaml
call conda activate book_ai
call python -m spacy download en_core_web_sm
call conda deactivate