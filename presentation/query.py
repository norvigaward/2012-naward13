import parser

def get_words(expr):
	if isinstance(expr, parser.Intersection) or isinstance(expr, parser.Union):
		return reduce(list.__add__, map(get_words, expr.exprs), [])
	elif isinstance(expr, parser.Complement):
		return get_words(expr.expr)
	elif isinstance(expr, parser.Word):
		return [expr.word]
	else:
		raise Exception('Unhandled case')
		
def select_all_pairs(words):
	w = '("' + '","'.join(words) + '")'

	return '''
		SELECT DISTINCT b AS word FROM pairs WHERE a IN {0}
		'''.format(w)
		
# 		UNION
#		SELECT a AS word FROM pairs WHERE b IN {0}

def select_pairs_with_score(word):
	return '''
		SELECT DISTINCT b AS word, count as score FROM pairs WHERE a="{0}"
		'''.format(word)

#		UNION
#		SELECT a AS word, (1.0 * count) / (SELECT SUM(count) #FROM pairs WHERE a="{0}" OR b="{0}") as score FROM pairs WHERE #b="{0}"		

def join_pairs(words):
	query = '(' + select_all_pairs(words) + ') AS root'
	for word in words:
		query += ' LEFT JOIN (' + select_pairs_with_score(word) + ') AS _' + word + '_ ON root.word = _' + word + '_.word'
	return query
	
def compute_score(expr):
	if isinstance(expr, parser.Intersection):
		return '(' + ' * '.join(map(compute_score, expr.exprs)) + ')'
	elif isinstance(expr, parser.Union):
		print expr.exprs
		return '((' + ' + '.join(map(compute_score, expr.exprs)) + ') / ' + str(len(expr.exprs)) + ')'
	elif isinstance(expr, parser.Complement):
		return '(1.0 - ' + compute_score(expr.expr) + ')'
	elif isinstance(expr, parser.Word):
		return 'coalesce(_' + expr.word + '_.score, 0.0)'		# Note, coalesce constant (= default for NULL values in left join) affects result 
	else:
		raise Exception('Unhandled case')

def compute_scores(matrix, expr):
	result = None

	if isinstance(expr, parser.Intersection):
		for subexpr in expr.exprs:
			t = compute_scores(matrix, subexpr)
			if result == None:
				result = t
			else:
				for word in result:
					result[word] *= t[word]
	elif isinstance(expr, parser.Union):
		for subexpr in expr.exprs:
			t = compute_scores(matrix, subexpr)
			if result == None:
				result = t
			else:
				for word in result:
					result[word] += t[word]
		for word in result:
			result[word] /= len(expr.exprs)
	elif isinstance(expr, parser.Complement):
		result = compute_scores(matrix, expr.expr)
		for word in result:
			result[word] = 1.0 - result[word]
	elif isinstance(expr, parser.Word):
		result = {}
		for word in matrix:
			if expr.word in matrix[word]:
				result[word] = matrix[word][expr.word]
			else:
				result[word] = 0.0
	else:
		raise Exception('Unhandled case')
	return result

def make_query(expr, limit):
	words = set(get_words(expr))
	return 'SELECT root.word AS word, ' + compute_score(expr) + ' AS total_score FROM ' + join_pairs(words) + ' WHERE total_score > 0 AND root.word NOT IN ("' + '","'.join(map(str, words)) + '") ORDER BY total_score DESC LIMIT ' + str(limit)
	
