import math
import matplotlib.patches as mpatches
import matplotlib.pyplot as plt
from   python_speech_features import mfcc
from python_speech_features import logfbank
import scipy.io.wavfile as wav
from   DTWW.CDTWQuery import C_DTWQuery2Query
import os
import numpy as np
import processOutput as po
import wave
import contextlib
import operator
from scipy.interpolate import InterpolatedUnivariateSpline

def genMFCC_Feature(wavefilename):
    (rate,sig) = wav.read(wavefilename)
    mfcc_feat = mfcc(sig,rate)
    (nr,nc) = mfcc_feat.shape
    mfcc_feat = mfcc_feat[:,1:nc]
    return mfcc_feat.transpose()


def genFBank_Feature(wavefilename):
    (rate,sig) = wav.read(wavefilename)
    fbank_feat = 10*logfbank(sig, rate)
    (nr,nc) = fbank_feat.shape
    return  fbank_feat.transpose()


def getDuration(fileName):
    with contextlib.closing(wave.open(fileName,'r')) as f:
        frames = f.getnframes()
        rate = f.getframerate()
        duration = frames / float(rate)
        return duration


def getMedian(directory):
    arr={}
    for root,directories,files in os.walk(directory):
        for f in files:
            if f.endswith(".wav"):
                arr[f] = getDuration(directory+"/"+f)
    sorted_arr = sorted(arr.items(), key = operator.itemgetter(1))
    arrSize = len(sorted_arr)
    index = (arrSize/2) - 1
    print sorted_arr[index][0] + " was chosen as the Median"
    return sorted_arr[index][0]

def getMode(directory):
    arr={}
    for root,directories,files in os.walk(directory):
        for f in files:
            if f.endswith(".wav"):
                arr[f] = getDuration(directory+"/"+f)
    sorted_arr = sorted(arr.items(), key = operator.itemgetter(1))
    arrSize = len(sorted_arr)
    index = (arrSize) - 1
    print sorted_arr[index][0] + " was chosen as the Mode"
    return sorted_arr[index][0]

def performOp(directory):
    colors = ['b','g','r','c','m','y','k','purple','pink','orange','maroon']
    # colors = ['b','r','b','r','g','g','g','r','g','r','r']
    # colors = ['w','w','w','r','w','w','w','r','w','r','r']
    prosody =  po.processOutput('outputFile.txt')
    plt.axis([0, 90, 50, 300])
    plt.ion()
    finalPlot = []
    query1 = genFBank_Feature(directory+'/'+getMode(directory))
    for root, directories, files in os.walk(directory):
        noOfIndices = len(files)
        index = 0
        classes = []
        for f in files:
            fname = f
            f = directory+'/'+f
            print f

            if f.endswith(".wav"):
                query2       = genFBank_Feature(f)
                # finalPlot[index] = {}
                demoQuery2Query             = 1
                if (demoQuery2Query):
                    myDTQQuery2Query        = C_DTWQuery2Query(query1,query2)
                    DistMatrix              = myDTQQuery2Query.get_DistMatrix()
                    dtwMatrix               = myDTQQuery2Query.get_dtwMatrix()
                    dtwTraceForward         = myDTQQuery2Query.get_TraceForwardMatrix()
                    actTraceBackMatrix      = myDTQQuery2Query.get_TraceBackMatrix()
                    #f, (ax3) = plt.subplots(1, sharex=True, sharey=True)
                    plt.figure(1)
                    # ax1.imshow(dtwMatrix, interpolation='nearest')
                    # ax2.imshow(dtwTraceForward, interpolation='nearest')
                    # ax3.imshow(actTraceBackMatrix, interpolation='nearest')
                    index = index + 1
                    temp = 0
                    harmonic = 1
                    print "index of file: " + f + " is : "+ str(index)
                    finalPlot.append(fname)
                    prosodyF = prosody[index-1]
                    print len(actTraceBackMatrix)
                    for key, prosodyValue in prosodyF.iteritems():
                        print str(key) + " : " + str(prosodyValue)
                        if(key<len(actTraceBackMatrix)):
                            xx = np.where(actTraceBackMatrix[:,key]==1)[0]
                            print xx
                            for t in xx:
                                plt.xlim(0,100)
                                plt.ylim(50,1000)
                                # print "Log of " + str(prosodyValue) + " is " + str(math.log(prosodyValue))
                                print "Difference of " + str(abs(prosodyValue-float(temp)))
                                # plt.scatter(t,math.log(prosodyValue),color = colors[index-1],s=2)
                                plt.scatter(t,harmonic*prosodyValue,color = colors[index-1],s=2)
                            # temp = prosodyValue

                        # finalPlot[index][xx[0]] = prosodyValue
                        # print str(prosodyValue) + " at the x coordinate of : " + str(xx[0][0])
                    # print actTraceBackMatrix
                    # f.subplots_adjust(hspace=0)
                    # plt.show()
                    # plt.legend((handle[0],handle[1]),('1','2'),scatterpoints = 1,loc = 'upper right')
                    # classes += f
                    # class_colours = ['r','b','g']
                    recs = []
                    for i in range(0, index):
                        recs.append(mpatches.Rectangle((0, 0), 1, 1, fc=colors[i]))
                    plt.legend(recs, finalPlot, loc=1)
    while True:
        plt.pause(0.05)

if __name__ == "__main__":
    performOp("VADOutput")
