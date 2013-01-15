from itertools import combinations

def prepare_data(conn, query, min_trait_count, max_trait_count):
	words = query.split();

	conn.execute('DROP TABLE IF EXISTS visualization_tmp')
	conn.execute('CREATE TEMP TABLE visualization_tmp (word,trait,count)')	

	for word in words:
		sql_ranking_query = '''INSERT INTO visualization_tmp (word,trait,count)
					SELECT a,b,count FROM pairs
					WHERE a = "{0}"
					ORDER BY count DESC
					LIMIT {1}'''.format(word,max_trait_count)

		conn.execute(sql_ranking_query)

	conn.execute('''DELETE FROM visualization_tmp WHERE word IN (
				SELECT word FROM visualization_tmp
				GROUP BY word
				HAVING COUNT(trait) < {0}
			)'''.format(min_trait_count))	
	#conn.execute('CREATE INDEX wordind ON visualization_tmp (word)')

#flattens a list of pairs to a list of values
def flatten(result):
	return list(sum(result, ())) 

def get_words(conn):
	words = conn.execute('SELECT DISTINCT word FROM visualization_tmp')
	return flatten(words)

#------- node size stuff ------------
def get_sizes(conn):
	sql_size_query = 'SELECT word, SUM(count) AS size FROM visualization_tmp GROUP BY word ORDER BY SUM(count) DESC'
	cur = conn.execute(sql_size_query)
	cur.arraysize = 100
	return cur.fetchall()

def scale_sizes(sizes, min_target_size, max_target_size):
	max_size = float(sizes[0][1])
	return map(lambda (w,size):(w, size/max_size * (max_target_size - min_target_size) + min_target_size), sizes)


#------- top-k ranking stuff ------------
def get_rankings(conn, words):
	rankings = {}
	for word in words:
		sql_ranking_query = '''SELECT trait FROM visualization_tmp 
					WHERE word = "{0}" ORDER BY count DESC'''.format(word)
		rankings[word] = flatten(conn.execute(sql_ranking_query))

	return rankings

#------- link distance stuff ------------
def get_link_distances(rankings):
	link_pairs = get_link_pairs(rankings.keys())

	link_distances = map(lambda (w1,w2):(w1,w2, rbo_distance([w1] + rankings[w1],[w2] + rankings[w2]) ), link_pairs)
	link_distances.sort(link_distance_comparator)

	return link_distances

def link_distance_comparator((a,b,dist1), (c,d,dist2)):
	if dist1 > dist2: return -1
	elif dist1 < dist2: return 1
	else: return 0

def scale_link_distances(link_distances, min_link_distance, max_link_distance):
	return map(lambda (w1,w2,dist):(w1,w2,dist * (max_link_distance - min_link_distance) + min_link_distance), link_distances)

def get_link_pairs(words):
	return list(combinations(words,2))

def rbo_distance(ranking1,ranking2):
	total = 0;
	k = min(len(ranking1),len(ranking2));
	p = 0.8;

	xk = -1;
	for d in xrange(1,k+1):
		xd = float(len(set(ranking1[0:d]) & set(ranking2[0:d])))	
		
		if d == k:
			xk = xd
		
		total += xd/d * p**d
	
	return 1 - (xk/k * p**k + (1.0-p)/p * total)

def kendall_tau_distance(ranking1,ranking2):
	traits = list(set(ranking1) | set(ranking2)) #the set of all traits in both rankings
	traitslen = len(traits)

	#build reverse index from trait to rank
	ranking1_index = reverseRankIndex(ranking1)
	ranking2_index = reverseRankIndex(ranking2)

	totalPenalty = 0
	for i, trait1 in enumerate(traits):
		trait1_rank1 = ranking1_index.get(trait1)
		trait1_rank2 = ranking2_index.get(trait1)

		trait1_in_ranking1 = trait1_rank1 != None
		trait1_in_ranking2 = trait1_rank2 != None

		for j in xrange(i+1,traitslen):
			trait2 = traits[j]

			trait2_rank1 = ranking1_index.get(trait2)
			trait2_rank2 = ranking2_index.get(trait2)

			trait2_in_ranking1 = trait2_rank1 != None
			trait2_in_ranking2 = trait2_rank2 != None

			trait1_before_trait2_in_ranking1 = trait1_rank1 < trait2_rank1
			trait1_before_trait2_in_ranking2 = trait1_rank2 < trait2_rank2
			
			penalty = 0

			#case 1
			if (trait1_in_ranking1 and trait2_in_ranking1 and trait1_in_ranking2 and trait2_in_ranking2):
				if ((trait1_before_trait2_in_ranking1 and not trait1_before_trait2_in_ranking2) or
					(not trait1_before_trait2_in_ranking1 and trait1_before_trait2_in_ranking2)):
					penalty = 1

			#case 2
			if (trait1_in_ranking1 and trait2_in_ranking1 and 
				(trait1_in_ranking2 and not trait2_in_ranking2 or not trait1_in_ranking2 and trait2_in_ranking2)):
				
				if ((trait1_before_trait2_in_ranking1 and trait2_in_ranking2) or (not trait1_before_trait2_in_ranking1 and trait1_in_ranking2)):
					penalty = 1


			if (trait1_in_ranking2 and trait2_in_ranking2 and 
				(trait1_in_ranking1 and not trait2_in_ranking1 or not trait1_in_ranking1 and trait2_in_ranking1)):
				
				if ((trait1_before_trait2_in_ranking2 and trait2_in_ranking1) or (not trait1_before_trait2_in_ranking2 and trait1_in_ranking1)):
					penalty = 1


			#case 3
			if ((trait1_in_ranking1 and not trait2_in_ranking1 and not trait1_in_ranking2 and trait2_in_ranking2) or 
				(not trait1_in_ranking1 and trait2_in_ranking1 and trait1_in_ranking2 and not trait2_in_ranking2)):
				penalty = 1

			#case 4
			if ((trait1_in_ranking1 and trait2_in_ranking1 and not trait1_in_ranking2 and not trait2_in_ranking2) or 
				 (not trait1_in_ranking1 and not trait2_in_ranking1 and trait1_in_ranking2 and trait2_in_ranking2)): 
				penalty = 0

			totalPenalty += penalty

	return totalPenalty/(traitslen * (traitslen - 1)/2.0);

	
def reverseRankIndex(ranking):
	index = {}
	
	for i,trait in enumerate(ranking):
		index[trait] = i

	return index

