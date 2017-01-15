import numpy as np

from sklearn.cluster import DBSCAN
from sklearn import metrics
from sklearn.preprocessing import StandardScaler

sample_file = open("/home/xiaoyu/sample_formatted.txt")

sample = []
label = []

for line in sample_file:
    labeled_point = line.split(" ")
    features = []
    for feature in labeled_point[1:19]:
        features.append(float(feature.split(":")[1]))
    features.append(float(labeled_point[20].split(":")[1].replace('\n','')))
    sample.append(features)
    label.append(float(labeled_point[0]))

sample = StandardScaler().fit_transform(sample)
db = DBSCAN(eps=3, min_samples=10).fit(sample[0:10000])

core_samples_mask = np.zeros_like(db.labels_, dtype=bool)
core_samples_mask[db.core_sample_indices_] = True
labels = db.labels_

# Number of clusters in labels, ignoring noise if present.
n_clusters_ = len(set(labels)) - (1 if -1 in labels else 0)


print 'Estimated number of clusters: %d'%n_clusters_
print "Homogeneity: %0.3f"%metrics.homogeneity_score(label[0:10000], labels)
print "Completeness: %0.3f"%metrics.completeness_score(label[0:10000], labels)
print "V-measure: %0.3f"%metrics.v_measure_score(label[0:10000], labels)
print "Adjusted Rand Index: %0.3f"%metrics.adjusted_rand_score(label[0:10000], labels)
print "Adjusted Mutual Information: %0.3f"%metrics.adjusted_mutual_info_score(label[0:10000], labels)
print "Silhouette Coefficient: %0.3f"%metrics.silhouette_score(sample[0:10000], labels)