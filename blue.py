import numpy
import math
import matplotlib 
import scipy.io.wavfile
import pandas as pd
import matplotlib.pyplot as plt
from scipy import spatial
from scipy.fftpack import dct

def mfcc( sample_rate, signal ):
	pre_emphasis = 0.97
	signal = signal[0:int(1 * sample_rate)] 
	emphasized_signal = signal #numpy.append(signal[0], signal[1:] - pre_emphasis * signal[:-1])	
	frame_size = 0.025 
	frame_stride = 0.01

	frame_length, frame_step = frame_size * sample_rate, frame_stride * sample_rate #CONVERTING TO SAMPLES
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
	high_freq_mel = (2595 * numpy.log10(1 + (sample_rate / 2) / 700))  
	mel_points = numpy.linspace(low_freq_mel, high_freq_mel, nfilt + 2)
	hz_points = (700 * (10**(mel_points / 2595) - 1)) 
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
	mfcc = dct(filter_banks, type=2, axis=1, norm='ortho')[:, 1 : (num_ceps + 1)]
	cep_lifter=22
	(nframes, ncoeff) = mfcc.shape
	n = numpy.arange(ncoeff)
	#print numpy.shape(mfcc)
	lift = 1 + (cep_lifter / 2) * numpy.sin(numpy.pi * n / cep_lifter)
	mfcc *= lift 
	mfcc -= (numpy.mean(mfcc, axis=0) + 1e-8)
	return mfcc
	 
sample_rate2, signal2 = scipy.io.wavfile.read('Blue_MaleUS_Normal.wav')  
sample_rate3, signal3 = scipy.io.wavfile.read('Green_MaleUS_Normal.wav')  
sample_rate4, signal4 = scipy.io.wavfile.read('Yellow_MaleUS_Normal.wav')  

mfcc2 = mfcc( sample_rate2, signal2 );
mat = numpy.matrix(mfcc2)
with open('blue.txt','wb') as f:
    for line in mat:
        numpy.savetxt(f, line, fmt='%.2f')

mfcc3 = mfcc( sample_rate3, signal3 );
mat = numpy.matrix(mfcc3)
with open('green.txt','wb') as f:
    for line in mat:
        numpy.savetxt(f, line, fmt='%.2f')

mfcc4 = mfcc( sample_rate4, signal4 );
mat = numpy.matrix(mfcc4)
with open('yellow.txt','wb') as f:
    for line in mat:
        numpy.savetxt(f, line, fmt='%.2f')

