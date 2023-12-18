# spike-google-cal

This is a spike, i.e. a proof of concept that is not intended to be production code, to test integration of the STARTER-team03 code from
m23 with the API for Google Calendar.


In particular, we want to test whether we can configure one or more private google calendars (private within Google Calendar), but allow the app access to these so that it can retrieve information (read only) and display that information to authorized users.

So for this initial spike, we only need the API for reading calendar events (not creating them, at least not yet).  And the focus is on access to a private google calendar that the app is then authorized to access (but also access when the user owning the calendar is offline, meaning that we may need to (a) store access tokens, and/or (b) cache the calendar information).

The purpose of this spike is to determine the feasibility of using
Google Calendar as the place to store and visualize
* Driver Shift Availability
* Driver Shift Assignment
* Ride Requests

The idea is this:

## Driver Shifts:

* A central Google Calendar would be established for Admins to assign driver shifts.  
* Admins could do CRUD operations on that calendar directly in Google Calendar (meaning there is no need to have Create, Update, Delete operations directly in the app.) 
* Admins will annotate the Calendar entries with titles with a special limited syntax that would indicate the driver id, and where applicable, the backup driver id for each shift.
* The app would be able to retrieve data from this Google Calendar and display the shift table for a given week along with the driver information (name, email address), or in cases where the google calendar entry is misformed, an error message.

This allows admins to set up shifts that repeat, as well as setting up one-time exceptions, etc. all with the feature that Google Calendar provides.

The calendar could still be a private calendar and it would not be necessary for anyone to have access to the calendar except the Admin.  The read access to the calendar would be provided through the app.

The app only needs read access to this private calendar.

## Driver Availability

In the next phase, drivers would each create their own private google calendar that they would authorize the app to have read access to.

These calendar would be used by the admin to determine how to assign shifts, which at least initially, would still be assigned through the actual Google Calendar interface.

# Resources

See also: 
* <https://www.aurigait.com/blog/accessing-google-calendar-data-using-google-calendar-api/>
* <https://stackoverflow.com/questions/42769550/google-calendar-api-within-spring-boot>
* <https://github.com/a2cart/google-calendar-api>
* <https://bhavsac.medium.com/google-calendar-java-api-integration-part-1-d1c89d083986>
* <https://blog.auryn.dev/posts/google-auth-java/>
* <https://www.youtube.com/watch?v=gMInK9daNUs>
* <https://stackoverflow.com/questions/58873622/googles-refresh-access-token-api-not-working-from-springboot>

For working with Google Calendar Events:
* <https://stackoverflow.com/questions/76141894/how-do-i-pass-a-google-calendar-event-via-a-spring-boot-controller-as-the-json-r>

# Versions
* Java: 17
* node: 16.20.0
See [docs/versions.md](docs/versions.md) for more information on upgrading versions.


# Setup before running application

Before running the application for the first time,
you need to do the steps documented in [`docs/oauth.md`](docs/oauth.md).

Otherwise, when you try to login for the first time, you 
will likely see an error such as:

<img src="https://user-images.githubusercontent.com/1119017/149858436-c9baa238-a4f7-4c52-b995-0ed8bee97487.png" alt="Authorization Error; Error 401: invalid_client; The OAuth client was not found." width="400"/>

# Getting Started on localhost

* Open *two separate terminal windows*  
* In the first window, start up the backend with:
  ``` 
  mvn spring-boot:run
  ```
* In the second window:
  ```
  cd frontend
  npm install  # only on first run or when dependencies change
  npm start
  ```

Then, the app should be available on <http://localhost:8080>

If it doesn't work at first, e.g. you have a blank page on  <http://localhost:8080>, give it a minute and a few page refreshes.  Sometimes it takes a moment for everything to settle in.

If you see the following on localhost, make sure that you also have the frontend code running in a separate window.

```
Failed to connect to the frontend server... On Dokku, be sure that PRODUCTION is defined.  On localhost, open a second terminal window, cd into frontend and type: npm install; npm start";
```

# Getting Started on Dokku

See: [/docs/dokku.md](/docs/dokku.md)

# Accessing swagger

To access the swagger API endpoints, use:

* <http://localhost:8080/swagger-ui/index.html>

Or add `/swagger-ui/index.html` to the URL of your dokku deployment.

# To run React Storybook

* cd into frontend
* use: npm run storybook
* This should put the storybook on http://localhost:6006
* Additional stories are added under frontend/src/stories

* For documentation on React Storybook, see: https://storybook.js.org/

# SQL Database access

On localhost:
* The SQL database is an H2 database and the data is stored in a file under `target`
* Each time you do `mvn clean` the database is completely rebuilt from scratch
* You can access the database console via a special route, <http://localhost:8080/h2-console>
* For more info, see [docs/h2-database.md](/docs/h2-database.md)

On Dokku, follow instructions for Dokku databases:
* <https://ucsb-cs156.github.io/topics/dokku/postgres_database.html>
