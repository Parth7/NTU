import numpy as np
from scipy import spatial

class SpringDTW:
    def __init__(self, q_feat, c_feat):
        """
        Initializes the object with the required values for performing DTW.
        :param q_feat: feature matrix of the corpus audio
        :param c_feat: feature matrix of the corpus audio
        """
        self.template = q_feat
        self.stream = c_feat
        
        self.n = len(self.template)
        self.m = len(self.stream)



    def accdist_calc_v2(self):
        #dist_matrix = np.ndarray(shape=(self.template.shape[0], self.stream.shape[0]), dtype=float)
        dist_matrix = spatial.distance.cdist(self.template, self.stream, metric='cosine')
        return dist_matrix
        
    def accdist_calc_v3(self):
        dist_matrix = np.ndarray(shape=(self.template.shape[0], self.stream.shape[0]), dtype=float)
        #dist_matrix = spatial.distance.cdist(self.template, self.stream, metric='cosine')
        for i in range(self.n):
            for j in range(self.m):
                dist_matrix[i][j] = spatial.distance.euclidean(self.template[i], self.stream[j])
        return dist_matrix
    def checkNaN(self, dist):
        for i in range(self.n):
            for j in range(self.m):
                if math.isnan(dist[i][j]):
                    return i,j
        #fig, ax = plt.subplots()
        #ax.matshow(dist_matrix, cmap=plt.cm.RdGy)
        #plt.show()
        return 0,0
    def DTW(self, dist):
        cost = np.ndarray(shape=(self.template.shape[0], self.stream.shape[0]), dtype=float)
        avg_cost = np.ndarray(shape=(self.template.shape[0], self.stream.shape[0]), dtype=float)
        length = np.ndarray(shape=(self.template.shape[0], self.stream.shape[0]), dtype=float)
        avg_cost[0][0] = dist[0][0];
        cost[0][0] = dist[0][0];
        length[0][0] = 1;
    
        for i in range(1,self.m):    
            length[0][i] = 1 + length[0][i-1];
            cost[0][i] = dist[0][i] + cost[0][i-1];
            avg_cost[0][i] = cost[0][i]/length[0][i];
        for i in range(1,self.n):
            length[i][0] = length[i-1][0] +1;
            cost[i][0] = dist[i][0] + cost[i-1][0];
            avg_cost[i][0]= cost[i][0]/length[i][0];
        for i in range(1, self.n):
            for j in range(1, self.m):
                cost_0 = dist[i][j]+cost[i-1][j];
                cost_1 = dist[i][j]+cost[i][j-1];
                cost_2 = dist[i][j]+cost[i-1][j-1];
                avg_cost_0 = cost_0/(1+length[i-1][j]);
                avg_cost_1 = cost_1/(1+length[i][j-1]);
                avg_cost_2 = cost_2/(1+length[i-1][j-1]);
                if avg_cost_0 < avg_cost_1:
                    if avg_cost_0 < avg_cost_2:
                        avg_cost[i][j] = avg_cost_0;
                        cost[i][j] = cost_0;
                        length[i][j] = 1 + length[i-1][j];
                    else:
                        avg_cost[i][j] = avg_cost_2;
                        cost[i][j] = cost_2;
                        length[i][j] = 1 + length[i-1][j-1];
                elif avg_cost_1 < avg_cost_2:
                    avg_cost[i][j] = avg_cost_1;
                    cost[i][j] = cost_1;
                    length[i][j] = 1 + length[i][j-1];
                else:
                    avg_cost[i][j] = avg_cost_2;
                    cost[i][j] = cost_2;
                    length[i][j] = 1 + length[i-1][j-1];
        return avg_cost #return avg_cost[self.n - 1][self.m - 1]             
