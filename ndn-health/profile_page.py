
from main_handler import *


class ProfilePage(MainHandler):
    def get(self):
        self.render('profile.html')

    def post(self):
        self.userType = self.request.get('type')

        # TODO - update type if altered