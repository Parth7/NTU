for k in range(D):
        for i in range(N):
	    for j in range(M):
                cost[i,j] = math.sqrt(sum((A[k,j]-B[k,i]))**2)
    distance_cost_plot(cost)
    accumulated_cost = numpy.zeros((N, M))
    #distance_cost_plot(accumulated_cost)
    for i in range(1, M):
	accumulated_cost[0,i] = distances[0,i] + accumulated_cost[0, i-1]
    #distance_cost_plot(accumulated_cost)
    for i in range(1, N):
    	accumulated_cost[i,0] = distances[i, 0] + accumulated_cost[i-1, 0] 
    for i in range(1, N):
    	for j in range(1, M):
            accumulated_cost[i, j] = min(accumulated_cost[i-1, j-1], accumulated_cost[i-1, j], accumulated_cost[i, j-1]) + distances[i, j]
    distance_cost_plot(accumulated_cost)
    return accumulated_cost[-1][-1]

import matplotlib.pyplot as plt
	offset = 5
	plt.xlim([-1, max(len(A), len(B)) + 1])
	plt.plot(A)
	plt.plot(B + offset)
	for (x1, x2) in path:
	plt.plot([x1, x2], [A[x1], B[x2] + offset])
	plt.show()

def MDTWDistance(s1, s2, window=10, num_columns=1):
    DTW={}

    w = max(window, abs(len(s1)-len(s2)))

    for i in range(-1,len(s1)):
        for j in range(-1,len(s2)):
            DTW[(i, j)] = float('inf')
    DTW[(-1, -1)] = 0

    for i in range(len(s1)):
        for j in range(max(0, i-w), min(len(s2), i+w)):
            #print "Finding Distance of", s1.loc[i], s2.loc[j]
            dist= mdist(s1.loc[i], s2.loc[j], num_columns)
            #print "Dist", dist
            #print i, j, dist
            DTW[(i, j)] = dist + min(DTW[(i-1, j)],DTW[(i, j-1)], DTW[(i-1, j-1)])

    return np.sqrt(DTW[len(s1)-1, len(s2)-1])

def mdist(a, b, num_col):
    dist = 0
    for col in range(num_col):
        #print "Finding Distance of", a[col], b[col]
        dist = dist + (a[col]-b[col])**2
    return dist

x=np.linspace(0,50,100)
ts1=pd.Series(3.1*np.sin(x/1.5)+3.5)
ts2=pd.Series(2.2*np.sin(x/3.5+2.4)+3.2)
ts3=pd.Series(0.04*x+8.0)
ts4=pd.Series(0.048*x+8.6)
ts5=pd.Series(-0.17*x+4.1)
ts6=pd.Series(-0.14*x+4.5)

ts1.plot()
ts2.plot()
ts3.plot()
ts4.plot()
ts5.plot()
ts6.plot()

plt.ylim(-4,12)
plt.legend(['ts1','ts2','ts3','ts4','ts5','ts6'])
plt.show()

timeSeries = pd.Panel({0:pd.DataFrame(np.transpose([ts1, ts2])),
                 1:pd.DataFrame(np.transpose([ts3, ts4])),
                   2:pd.DataFrame(np.transpose([ts5, ts6]))
                  })

print "0 and 1:",MDTWDistance(timeSeries[0], timeSeries[1],window=10, num_columns=2)
print "0 and 2:",MDTWDistance(timeSeries[0], timeSeries[2],window=10, num_columns=2)
print "1 and 2:",MDTWDistance(timeSeries[1], timeSeries[2],window=10, num_columns=2)
