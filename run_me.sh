#!/bin/bash

ctm_word=../align_fbank_word/ctm
ctm_subword=../align_fbank_word/ctm_phone/utt_phone.txt
lexicon=lexicon5.txt
phonesFile=phones.txt
outfile=result.txt

outdirSplit=tmp_dat
numOfJobs=32

mkdir -p $outdirSplit

java SplitCTM  $ctm_subword $ctm_word $numOfJobs $outdirSplit

for (( i = 0 ; i < $numOfJobs ; i++ ))
do
    java TimInforExtr tmp_dat/ctm_subword.$i tmp_dat/ctm_word.$i $lexicon $phonesFile results/result.$i.txt > log/log.$i.txt
    if [ "$(wc -l < tmp_dat/ctm_word.$i)" -eq "$(wc -l < results/result.$i.txt)" ]; then echo "Job $i Match!"; else echo "Warning job $i mismatch!"; fi
done

java TimInforExtr_manualProcess tmp_dat/ctm_subword.9 tmp_dat/ctm_word.9 $lexicon $phonesFile results/result.9.txt > log/log.9.manual.txt
java TimInforExtr_manualProcess tmp_dat/ctm_subword.10 tmp_dat/ctm_word.10 $lexicon $phonesFile results/result.10.txt > log/log.10.manual.txt

for (( i = 0 ; i < $numOfJobs ; i++ ))
do
    cat results/result.$i.txt >> results/final_result.txt
done


