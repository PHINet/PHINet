from google.appengine.ext import db

"""
The fundamental User data type.
"""


class User(db.Model):
    userName = db.StringProperty(required=True)
    userPw = db.StringProperty(required=True)
    userType = db.StringProperty(required=True)
    userEmail = db.StringProperty()

    # TODO - allow users to have score/karma
    # and place restriction/privileges based upon this

    ## NOTE: "cls" is python convention that takes the place of "self" for class methods

    @classmethod
    def byId(cls, uid):
        return User.get_by_id(uid)  # GAE method

    @classmethod
    def byName(cls, name):
        # similar query to "select * from User where User.name = name"
        return User.all().filter('userName =', name).get()

    @classmethod
    def register(cls, name, pw, userType, email=None):

        print ("usertype: ", userType)

        return User(userName=name, userPw=pw, userEmail=email, userType=userType)

    @classmethod
    def login(cls, name, pw):
        user = cls.byName(name)
        if user and user.userPw == pw:
            return user