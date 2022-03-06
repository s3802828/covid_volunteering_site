# COVID VOLUNTEERING SITES MANAGEMENT ANDROID APP

Android app to manage the number of volunteers and information of covid volunteering sites.

Contents
========
* [Functionalities of the app](#functionalities of the app)
* [Technologies](#technologies)

### Functionalities of the app
---

	General functionalities (all types of users could):
		- Login, Sign Up, Logout
		- View nearby sites on map (when user zoom in, nearer sites to current camera target would be shown and zoom out, further sites would be shown)
		- View sites’ information (guest user and unregistered user (logged in but hasn’t joined that site) can only see basic information, whereas leader, joined volunteers, and super users could see more details)
		- Route from current location to one site. (could be done by clicking on the marker of one site)
		- Search for a site based on site name (default) or site leader name.
	Site:
		- Add New Site (current user become leader of the site) (could be done by clicking on the map or click on the floating action button at the bottom app bar)
		- Modify Sites (could only be done by leader of that site or super users)
		- Join Sites/Register for friends (only leader and volunteers could, super users are not allowed to) (could be done by clicking on the marker of one site or click on the join item at the bottom app bar)
		- View volunteers list and Download that list (only leader of that site or super users are allowed to)
	User:
		- Receive notifications about new volunteers have joined their lead sites or if there is any modifications to their joined sites.
		- Super User: could only view and modify all of the site information and also the general functionalities.

### Technologies
---

	- Google Play Service Map and Location: work with map and get current location
	- Direction API: get route from current location to one site
	- SQLite Database: store site, user, volunteering, and notification data.
	- Other Android components/built-in classes: Activities, DatabaseHelper (to query the database), Notification (to send notification)
	- UI Components: Layouts, EditText, TextView, Button, BottomAppBar, SearchView, FloatingActionButton, Dialogs, RadioGroup & RadioButton: to build up the UI for the app
