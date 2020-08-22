#!/usr/bin/python
#backup_final.py
#Parth Parakh, 21th Feb'18 19:23
#purpose: to compare the input wavefile with precomputed templates and determine the colour.

#---------------------------------------------------------------------
#import modules
import numpy
import matplotlib
import scipy.io.wavfile
import matplotlib.pyplot as plt
from scipy import spatial
from scipy.fftpack import dct
from multiprocessing.dummy import Pool as ThreadPool

#------------------------------------------------------------------------
#calculates mfcc by first preemphasing signal then calculating fast fourier transform and finally fbank features 
#returns a feature matrix 
def mfcc(sample_rate, signal):
    pre_emphasis = 0.97
    signal = signal[0:int(1 * sample_rate)]
    emphasized_signal = numpy.append(signal[0], signal[1:] - pre_emphasis * signal[:-1])
    frame_size = 0.025
    frame_stride = 0.01

    frame_length, frame_step = frame_size * sample_rate, frame_stride * sample_rate  # CONVERTING TO SAMPLES
    signal_length = len(emphasized_signal)
    frame_length = int(round(frame_length))
    frame_step = int(round(frame_step))
    num_frames = int(numpy.ceil(float(numpy.abs(signal_length - frame_length)) / frame_step))

    pad_signal_length = num_frames * frame_step + frame_length
    z = numpy.zeros((pad_signal_length - signal_length))
    pad_signal = numpy.append(emphasized_signal, z)

    indices = numpy.tile(numpy.arange(0, frame_length), (num_frames, 1)) + numpy.tile(
        numpy.arange(0, num_frames * frame_step, frame_step), (frame_length, 1)).T
    frames = pad_signal[indices.astype(numpy.int32, copy=False)]

    frames *= numpy.hamming(frame_length)
    NFFT = 512
    mag_frames = numpy.absolute(numpy.fft.rfft(frames, NFFT))
    pow_frames = ((1.0 / NFFT) * ((mag_frames) ** 2))

    nfilt = 40
    low_freq_mel = 0
    high_freq_mel = (2595 * numpy.log10(1 + (sample_rate / 2) / 700))
    mel_points = numpy.linspace(low_freq_mel, high_freq_mel, nfilt + 2)
    hz_points = (700 * (10 ** (mel_points / 2595) - 1))
    bin = numpy.floor((NFFT + 1) * hz_points / sample_rate)

    fbank = numpy.zeros((nfilt, int(numpy.floor(NFFT / 2 + 1))))
    for m in range(1, nfilt + 1):
        f_m_minus = int(bin[m - 1])
        f_m = int(bin[m])
        f_m_plus = int(bin[m + 1])

        for k in range(f_m_minus, f_m):
            fbank[m - 1, k] = (k - bin[m - 1]) / (bin[m] - bin[m - 1])
        for k in range(f_m, f_m_plus):
            fbank[m - 1, k] = (bin[m + 1] - k) / (bin[m + 1] - bin[m])
    filter_banks = numpy.dot(pow_frames, fbank.T)
    filter_banks = numpy.where(filter_banks == 0, numpy.finfo(float).eps, filter_banks)
    filter_banks = 20 * numpy.log10(filter_banks)
    num_ceps = 12
    mfcc = dct(filter_banks, type=2, axis=1, norm='ortho')[:, 1: (num_ceps + 1)]
    cep_lifter = 22
    (nframes, ncoeff) = mfcc.shape
    n = numpy.arange(ncoeff)
    # print numpy.shape(mfcc)
    lift = 1 + (cep_lifter / 2) * numpy.sin(numpy.pi * n / cep_lifter)
    mfcc *= lift
    mfcc -= (numpy.mean(mfcc, axis=0) + 1e-8)
    return mfcc

#-----------------------------------------------------------------------------
# calculates distance matrix based on cosine distance
def generates_distances2D(x, y):
    dist_matrix = spatial.distance.cdist(x, y, metric='cosine')
    return dist_matrix

#-------------------------------------------------------------------------------
# used to calculate cost matrix from distance matrix
def generates_accumulated_cost_p(x, y, distances):
    accumulated_cost = numpy.zeros((len(x), len(y)))

    accumulated_cost[0, 0] = distances[0, 0]

    for i in range(1, len(x)):
        accumulated_cost[i, 0] = distances[i, 0] + accumulated_cost[i - 1, 0]

    for j in range(1, len(y)):
        accumulated_cost[0, j] = distances[0, j] + accumulated_cost[0, j - 1]

    for i in range(1, len(x)):
        for j in range(1, len(y)):
            accumulated_cost[i, j] = min(accumulated_cost[i - 1, j - 1], accumulated_cost[i - 1, j],
                                         accumulated_cost[i, j - 1]) + distances[i, j]

    return accumulated_cost

#------------------------------------------------------------------------------
# plots function
def distance_cost_plot(distances, xlabel="x", ylabel="y", all_ticks=False, figure=True):
    distances = distances.T
    if figure:
        plt.figure(figsize=(10, 10))
    plt.imshow(distances, interpolation='nearest', cmap='Reds')
    plt.gca().invert_yaxis()
    plt.ylabel(ylabel)
    plt.xlabel(xlabel)
    if all_ticks:
        plt.xticks(range(distances.shape[1]), range(distances.shape[1]))
        plt.yticks(range(distances.shape[0]), range(distances.shape[0]))
    plt.grid()
    plt.colorbar();


#------------------------------------------------------------------------------
# minimum distance cost path between the template and input file is calculated
def path_cost_p(x, y, accumulated_cost, distances):
    path = [[len(x) - 1, len(y) - 1]]
    cost = 0

    i = len(x) - 1
    j = len(y) - 1

    while i > 0 or j > 0:
        if i == 0:
            j = j - 1
        elif j == 0:
            i = i - 1
        else:
            if accumulated_cost[i - 1, j] == min(accumulated_cost[i - 1, j - 1], accumulated_cost[i - 1, j],
                                                 accumulated_cost[i, j - 1]):
                i = i - 1
            elif accumulated_cost[i, j - 1] == min(accumulated_cost[i - 1, j - 1], accumulated_cost[i - 1, j],
                                                   accumulated_cost[i, j - 1]):
                j = j - 1
            else:
                i = i - 1
                j = j - 1
        path.append([i, j])
    for [x, y] in path:
        cost = cost + distances[x, y]
    return path, cost

#----------------------------------------------------------------------------------
# parent function which calls all the other subfunctions to calculate the dtw between the input wav file and templates
def dtw(seqB):
    seqA = mfcc1.T;
    dist = generates_distances2D(seqA.T, seqB.T)
    acc_cost = generates_accumulated_cost_p(seqA.T, seqB.T, dist)
    path, cost = path_cost_p(seqA.T, seqB.T, acc_cost, dist)
    path = numpy.array(path)
    distance_cost_plot(acc_cost)
    plt.plot(path[:,0],path[:,1],'o-')
    plt.xlim(0,path[0,0])
    plt.ylim(0,path[0,1])
    plt.show()
    return cost

#-----------------------------------------------------------------------------------
# main caller
sample_rate1, signal1 = scipy.io.wavfile.read('jon_yellow.wav')
mfcc2 = numpy.loadtxt('blue.txt')
mfcc3 = numpy.loadtxt('green.txt')
mfcc4 = numpy.loadtxt('yellow.txt')
mfcc1 = mfcc(sample_rate1, signal1);
inputs = [mfcc2.T, mfcc3.T, mfcc4.T]
pool = ThreadPool(3)
results = pool.map(dtw, inputs)
# print results
pool.close()
pool.join()

ans = 10000;
for i in results:
    if i < ans:
        ans = i
        j = results.index(i)

print( ans )
