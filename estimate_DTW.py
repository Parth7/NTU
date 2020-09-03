import math
import os
import random
import sys
import time
import datetime
from scipy import spatial
from spring_Tung_v2 import SpringDTW
import numpy as np
import matplotlib.pyplot as plt

mat1 = np.loadtxt("outfile_mfcc1.txt");
mat2 = np.loadtxt("outfile_mfcc2.txt");

parth_dis= np.loadtxt("outfile.txt")

dis_type=sys.argv[1]
who = sys.argv[2]


sp = SpringDTW(mat1, mat2)


##Show Tung's calculated distance
if who == "Parth1":
	if dis_type == "Euc":
		print("Euclidean distance")
		dist_matrix = sp.accdist_calc_v3()
	else:
		print("Apply Cosine distance")
		dist_matrix = sp.accdist_calc_v2()
	avg_dis = sp.DTW(dist_matrix)
	np.savetxt("frame_by_frame_distance_with" + dis_type + "_frameDist.txt", dist_matrix, fmt='%.8f') 
	np.savetxt("length_normalized_distance_with" + dis_type + "_frameDist.txt", avg_dis, fmt='%.8f')  
	fig, ax = plt.subplots()
	ax.set_title("Frame-by-frame distance matrix")
	ax.matshow(dist_matrix, cmap=plt.cm.RdGy)
	
	fig, ax = plt.subplots()
	ax.set_title("Length normalized distance matrix")
	ax.matshow(avg_dis, cmap=plt.cm.RdGy)
	plt.show()

if who == "Parth2":
	avg_dis_parth = sp.DTW(parth_dis)
	##Show Parth's calculated distance
	fig, ax = plt.subplots()
	ax.set_title("Parth's calculated distance")
	ax.matshow(parth_dis, cmap=plt.cm.RdGy)
	
	fig, ax = plt.subplots()
	ax.set_title("Parth's length normalized distance matrix")
	ax.matshow(avg_dis_parth, cmap=plt.cm.RdGy)
	plt.show()
	
