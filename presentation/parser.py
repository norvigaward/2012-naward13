import re
import collections

# expr_a = expr_b+ ('|' expr_b+)* EOF
# expr_b
#	= WORD
#	| '-' expr_a
#	| '(' expr_a ')

class Intersection:
	def __init__(self, exprs):
		self.exprs = exprs
		
	def __str__(self):
		return '(' + ' '.join(map(str, self.exprs))	+ ')'
		
	def simplify(self):
		if len(self.exprs) == 1:
			return self.exprs[0].simplify()
		else:
			return self

class Union:
	def __init__(self, exprs):
		self.exprs = exprs
		
	def __str__(self):
		return '(' + ' | '.join(map(str, self.exprs)) + ')'	

	def simplify(self):
		if len(self.exprs) == 1:
			return self.exprs[0].simplify()
		else:
			return self

class Complement:
	def __init__(self, expr):
		self.expr = expr

	def __str__(self):
		return '-' + str(self.expr.simplify())
		
	def simplify(self):
		return self

class Word:
	def __init__(self, word):
		self.word = word
		
	def __str__(self):
		return self.word	
		
	def simplify(self):
		return self

def lex(string):
	string = collections.deque(string)
	tokens = collections.deque()
	
	while len(string) > 0:
		if string[0] == ' ':
			string.popleft()
		elif string[0] == '-':
			tokens.append('-')
			string.popleft()
		elif string[0] == '(':
			tokens.append('(')
			string.popleft()
		elif string[0] == ')':
			tokens.append(')')
			string.popleft()
		elif string[0] == '+':
			tokens.append('+')
			string.popleft()
		elif string[0] == '.':
			tokens.append('.')
			string.popleft()
		elif string[0] >= 'a' and string[0] <= 'z':
			word = ""
			while string[0] >= 'a' and string[0] <= 'z': 
				word = word + string.popleft()
			tokens.append(word)
		else:
			raise Exception('Lexer error')
	return tokens
		

def parse(tokens):
	def accept():
		return tokens.popleft()
		
	def accept_if(token):
		if accept() != token:
			raise Exception('Parse error') 

	def parse_a():
		union = []
		while tokens[0] != '.' and tokens[0] != ')':
			intersection = []
			while tokens[0] != '+' and tokens[0] != '.' and tokens[0] != ')':
				intersection.append(parse_b())
			if tokens[0] == '+':
				accept()
			union.append(Intersection(intersection))
		return Union(union)
	
	def parse_b():
		if tokens[0] == '-':
			accept()
			result = Complement(parse_a())
		elif tokens[0] == '(':
			accept()
			result = parse_a()
			accept_if(')')
		elif re.match('[a-z]+', tokens[0]):
			result = Word(accept())
		else:
			raise Exception("Parse error")
		return result
	
	return parse_a()
