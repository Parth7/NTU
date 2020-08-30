from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

import math
import os
import random
import sys
import time
from scipy import spatial
import matplotlib.pyplot as plt
import numpy as np

dist = np.loadtxt(sys.argv[1])
fig, ax = plt.subplots()
ax.set_title(sys.argv[1])
ax.matshow(dist, cmap=plt.cm.RdGy)
plt.show()

