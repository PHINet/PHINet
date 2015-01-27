import jinja2
import os
import re
import webapp2

templateDir = os.path.join(os.path.dirname(__file__), 'templates')
jinjaEnv = jinja2.Environment(loader=jinja2.FileSystemLoader(templateDir),autoescape=True)


def init():
    global CACHE
    CACHE = {}  # CACHE is populated in main.py


def validUsername(username):
    # username between 3-20 characters
    return username and re.match(r"^[a-zA-Z0-9_-]{3,20}$", username)


def validPassword(password):
    # password between 3-20 characters

    # TODO - require more complex pw

    return password and re.match(r"^.{3,20}$", password)


def validEmail(email):
    # email is not required
    return not email or re.match(r'^[\S]+@[\S]+\.[\S]+$', email)


def renderStr(template, **params):
    html = jinjaEnv.get_template(template)
    return html.render(params)