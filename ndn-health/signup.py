
from main_handler import *

"""
File contains Signup and Login Handlers.
"""


class Signup(MainHandler):
    def generateNewUser(self):
        user = User.byName(self.username)

        if user:  # check to see whether user exists
            params = dict(username=self.username, email=self.email, password=self.password, verify=self.verify,
                          userType=self.userType)
            self.render('signupForm.html', errorUsername='User already exists.', **params)
        else:
            user = User.register(self.username, self.password, self.userType, self.email)
            user.put()  # places user object into the data store
            self.login(user)
            self.redirect('/')

    def get(self):
        self.render('signupForm.html')

    def post(self):
        invalidInput = False
        self.username = self.request.get('username')
        self.password = self.request.get('password')
        self.verify = self.request.get('verify')
        self.email = self.request.get('email')
        self.userType = self.request.get('type')

        # "params" variable stores what will populate the html
        params = dict(username=self.username, email=self.email, userType=self.userType)

        if not validUsername(self.username):
            params['errorUsername'] = "Invalid username."
            invalidInput = True

        if not validEmail(self.email):
            params['errorEmail'] = "Invalid email."
            invalidInput = True

        if not validPassword(self.password):
            params['errorPassword'] = "Invalid password."
            invalidInput = True
        elif self.password != self.verify:
            params['errorVerify'] = "Passwords didn't match."
            invalidInput = True

        if invalidInput:
            self.render('signupForm.html', **params)
        else:
            self.generateNewUser()


class Login(MainHandler):
    def get(self):
        self.render('loginForm.html')

    def post(self):
        username = self.request.get('username')
        password = self.request.get('password')

        user = User.login(username, password)

        if user:
            self.login(user)
            self.redirect('/')
        else:
            self.render('loginForm.html', error='Invalid Login.')

        # TODO - check for both username and email