
from utils import *
from user import User

"""
Base class for all other handlers.
"""


class MainHandler(webapp2.RequestHandler):
    def render(self, template, **kw):
        kw['user'] = self.user  # allows users to remain logged in
        self.response.out.write((renderStr(template, **kw)))

    def login(self, user):
        # sets cookie based upon .id() - a GAE method for data
        self.response.headers.add_header(
            'Set-Cookie', '%s=%s; Path=/' % ('userId', str(user.key().id())))

    def logout(self):
        # sets the cookie to a blank value
        self.response.headers.add_header('Set-Cookie', 'userId=; Path=/')

    def initialize(self, *a, **kw):
        # NOTE: GAE calls this prior to all loads,
        # here, it checks to see if user is logged in

        webapp2.RequestHandler.initialize(self, *a, **kw)
        uid = self.request.cookies.get('userId')

        self.user = None  # prior to login-cookie check

        try:
            if uid:
                self.user = User.byId(int(uid))  # self.user is valid login
        except Exception:
            pass  # bad cookie; self.user should remain None