#!/usr/bin/python
import sys
import cherrypy
import jinja2
import os
import sqlite3
import time
import re
import collections
import operator

import parser
import visualization
from query import make_query, get_words, compute_scores

env = jinja2.Environment(loader=jinja2.FileSystemLoader(os.path.abspath(os.path.join(os.path.dirname(__file__), 'templates'))))

def make_matrix(cur, words):
	matrix = {}
	for word in words:
		cur.execute('SELECT b, count FROM pairs WHERE a="' + word + '";')
		for (b, count) in cur.fetchall():
			if not b in matrix:
				matrix[b] = {}
			matrix[b][word] = count
	for word in words:
		if word in matrix:
			del matrix[word]
	return matrix

class Traitor(object):
    @cherrypy.expose
    def index(self):
        tmpl = env.get_template('index.html')
        return tmpl.render()
		
    @cherrypy.expose
    def search(self, query="", limit=100):
        tmpl = env.get_template('search.html')

        pairs = []
        dtime = 0.0

        print(query)

        if query:
            try:
                start = time.time()

                parsed_query = parser.parse(parser.lex(query + '.')).simplify()
                #sql_query = make_query(parsed_query, limit)
                #print sql_query

                conn = sqlite3.connect('pairs.db')
                cur = conn.cursor()
		cur.arraysize = 100
                #cur.execute(sql_query)
                #pairs = cur.fetchall()

                matrix = make_matrix(cur, get_words(parsed_query))
                pairs = compute_scores(matrix, parsed_query)

                filtered_pairs = {}
                for word in pairs:
                    if pairs[word] > 0.5:
                        filtered_pairs[word] = pairs[word]
                pairs = filtered_pairs

                pairs = sorted(pairs.iteritems(), key=operator.itemgetter(1), reverse = True)
                pairs = pairs[0:100]

                stop = time.time()
                dtime = stop - start
            except Exception as e:
                print "Oops! Something bad happened! (Error {0})".format(e)
                pairs = []
                sys.exc_clear()
    
        return tmpl.render(pairs=pairs, query=query, time=dtime, limit=int(limit))
    
    @cherrypy.expose
    def common(self):
        tmpl = env.get_template('common.html')

        start = time.time()
        conn = sqlite3.connect('pairs.db')
        query = 'SELECT a,SUM(count) as count FROM pairs GROUP BY a ORDER BY count DESC LIMIT 20'
        pairs = conn.execute(query).fetchall()
        stop = time.time()

        return tmpl.render(pairs=pairs, query="", time=(stop - start))
        
    @cherrypy.expose
    def visualize(self,query="",debug=""):
		tmpl = env.get_template('visualize.html')

		sizes = []
		rankings = []
		link_distances = []
        	dtime = 0.0

		if query:
			    try:
				start = time.time()

				conn = sqlite3.connect('pairs.db')

				visualization.prepare_data(conn, query,50,100)
				
				#get list of accepted words
				words = visualization.get_words(conn) 

				#update query string (optional and maybe not user friendly)
				query = ' '.join(words)

				sizes = visualization.get_sizes(conn)
				sizes = visualization.scale_sizes(sizes,5,50)	

				rankings = visualization.get_rankings(conn, words)

				link_distances = visualization.get_link_distances(rankings)
				
				#filter pairs that have less than 5% in common
				link_distances = filter(lambda (w1,w2,dist):dist < 0.95, link_distances)

				#square distances to increase visible distance effect
				link_distances = map(lambda (w1,w2,dist):(w1,w2,dist ** 2), link_distances)

				link_distances = visualization.scale_link_distances(link_distances,0,150)

				stop = time.time()
				dtime = stop - start
			    except Exception as e:
				print "Oops! Something bad happened! (Error {0})".format(e)
				sys.exc_clear()
    
        	return tmpl.render(sizes=sizes, rankings=rankings, link_distances=link_distances, query=query, time=dtime, debug=debug)
		
    @cherrypy.expose
    def about(self):
		tmpl = env.get_template('about.html')
		return tmpl.render()

cfg = {
        '/static': {
                'tools.staticdir.on': True,
                'tools.staticdir.dir': os.path.abspath(os.path.join(os.path.dirname(__file__), 'static'))
        },
        'global': {
                'server.socket_host': "127.0.0.1",
                'server.socket_port': 41058,
                'server.thread_pool': 10
        }
}

if __name__ == '__main__':
	cherrypy.quickstart(Traitor(), config=cfg)
else:
	application = cherrypy.Application(Traitor(), script_name=None, config=cfg)
