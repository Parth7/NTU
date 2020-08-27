import numpy
import math
import librosa
import sys
import pandas as pd
import seaborn as sns
import scipy.io.wavfile
import matplotlib.pyplot as plt
from scipy.fftpack import dct
from matplotlib import cm
from numpy.linalg import norm

def func( sample_rate, signal ):
	pre_emphasis = 0.97
	signal = signal[0:int(0.5 * sample_rate)] 
	emphasized_signal = numpy.append(signal[0], signal[1:] - pre_emphasis * signal[:-1])	
	frame_size = 0.025 
	frame_stride = 0.01

	frame_length, frame_step = frame_size * sample_rate, frame_stride * sample_rate
	signal_length = len(emphasized_signal)
	frame_length = int(round(frame_length))
	frame_step = int(round(frame_step))
	num_frames = int(numpy.ceil(float(numpy.abs(signal_length - frame_length)) / frame_step))

	pad_signal_length = num_frames * frame_step + frame_length
	z = numpy.zeros((pad_signal_length - signal_length))
	pad_signal = numpy.append(emphasized_signal, z) 

	indices = numpy.tile(numpy.arange(0, frame_length), (num_frames, 1)) + numpy.tile(numpy.arange(0, num_frames * frame_step, frame_step), (frame_length, 1)).T
	frames = pad_signal[indices.astype(numpy.int32, copy=False)]

	frames *= numpy.hamming(frame_length)
	NFFT=512
	mag_frames = numpy.absolute(numpy.fft.rfft(frames, NFFT))
	pow_frames = ((1.0 / NFFT) * ((mag_frames) ** 2))  

	nfilt = 40
	low_freq_mel = 0
	high_freq_mel = (2595 * numpy.log10(1 + (sample_rate / 2) / 700))  # Convert Hz to Mel
	mel_points = numpy.linspace(low_freq_mel, high_freq_mel, nfilt + 2)  # Equally spaced in Mel scale
	hz_points = (700 * (10**(mel_points / 2595) - 1))  # Convert Mel to Hz
	bin = numpy.floor((NFFT + 1) * hz_points / sample_rate)

	fbank = numpy.zeros((nfilt, int(numpy.floor(NFFT / 2 + 1))))
	for m in range(1, nfilt + 1):
	    f_m_minus = int(bin[m - 1])   # left
	    f_m = int(bin[m])             # center
	    f_m_plus = int(bin[m + 1])    # right

	    for k in range(f_m_minus, f_m):
		fbank[m - 1, k] = (k - bin[m - 1]) / (bin[m] - bin[m - 1])
	    for k in range(f_m, f_m_plus):
		fbank[m - 1, k] = (bin[m + 1] - k) / (bin[m + 1] - bin[m])
	filter_banks = numpy.dot(pow_frames, fbank.T)
	filter_banks = numpy.where(filter_banks == 0, numpy.finfo(float).eps, filter_banks)  # Numerical Stability
	filter_banks = 20 * numpy.log10(filter_banks)  # dB
	num_ceps = 12
	mfcc = dct(filter_banks, type=2, axis=1, norm='ortho')[:, 1 : (num_ceps + 1)] # Keep 2-13
	cep_lifter=22
	(nframes, ncoeff) = mfcc.shape
	n = numpy.arange(ncoeff)
	print numpy.shape(mfcc)
	lift = 1 + (cep_lifter / 2) * numpy.sin(numpy.pi * n / cep_lifter)
	mfcc *= lift 
	mfcc -= (numpy.mean(mfcc, axis=0) + 1e-8)
	return mfcc

def dtw(A, B, window = sys.maxint, d = lambda x,y: abs(x-y)):
	# create the cost matrix
	#A, B = numpy.array(A), numpy.array(B)
	M, N = len(A), len(B)
	cost = sys.maxint * numpy.ones((M, N))

	# initialize the first row and column
	cost[0, 0] = d(A[0], B[0])
	for i in range(1, M):
		cost[i, 0] = cost[i-1, 0] + d(A[i], B[0])

	for j in range(1, N):
		cost[0, j] = cost[0, j-1] + d(A[0], B[j])
		# fill in the rest of the matrix
	for i in range(1, M):
		for j in range(max(1, i - window), min(N, i + window)):
		    choices = cost[i - 1, j - 1], cost[i, j-1], cost[i-1, j]
		    cost[i, j] = min(choices) + d(A[i], B[j])

	# find the optimal path
	n, m = N - 1, M - 1
	path = []

	while (m, n) != (0, 0):
		path.append((m, n))
		m, n = min((m - 1, n), (m, n - 1), (m - 1, n - 1), key = lambda x: cost[x[0], x[1]])

	path.append((0,0))
	return cost[-1, -1], path

sample_rate1, signal1 = scipy.io.wavfile.read('jon_blue.wav')  
sample_rate2, signal2 = scipy.io.wavfile.read('Blue_MaleUS_Normal.wav')  
sample_rate3, signal3 = scipy.io.wavfile.read('Green_MaleUS_Normal.wav')  
sample_rate4, signal4 = scipy.io.wavfile.read('Yellow_MaleUS_Normal.wav')  

mfcc1 = func( sample_rate1, signal1 );
mfcc2 = func( sample_rate2, signal2 );
mfcc3 = func( sample_rate3, signal3 );
mfcc4 = func( sample_rate4, signal4 );

dist2= dtw(mfcc1.T, mfcc2.T, d=lambda x, y: abs(x - y))
print 'Normalized distance between the two sounds:', dist2

dist3= dtw(mfcc1.T, mfcc3.T, d=lambda x, y: abs(x - y))
print 'Normalized distance between the two sounds:', dist3

dist4= dtw(mfcc1.T, mfcc4.T, d=lambda x, y: abs(x - y))
print 'Normalized distance between the two sounds:', dist4

#print(mfcc)
#plt.plot(mfcc)
#fig, ax = plt.subplots()
#mfcc_data= numpy.swapaxes(mfcc, 0 ,1)
#cax = ax.imshow(mfcc_data, interpolation='nearest', cmap=cm.coolwarm, origin='lower')
#ax.set_title('MFCC')
#plt.show()

