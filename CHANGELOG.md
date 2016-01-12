# Changelog

## 1.0.5.2 (2016-01-12)

 - fix file upload issue when a user don't have a global READ role.
 - misc minor UI/UX improvements

## 1.0.5.1 (2015-11-16)

 - Fix XSS issue in the file upload section (require an authenticated user)

## 1.0.5 (2015-11-07)

 - UI/UX improvements
   - Change due date color based on the current date
   - Card label: on mouse hover, show a tooltip with the card name
   - Email notification: now the user can choose to receive only changes made by other users

## 1.0.4.1 (2015-10-19)

 - update dependencies ([spring framework](https://spring.io/blog/2015/10/15/spring-framework-4-2-2-4-1-8-and-3-2-15-available-now) due to a possible security issue)

## 1.0.4 (2015-09-15)

 - [#8](https://github.com/digitalfondue/lavagna/issues/8) implement gitlab support for gitlab.com and self hosted instances
 - updates some libraries
 - refactor part of the login system

## 1.0.3 (2015-09-01)

 - fix issue [#13](https://github.com/digitalfondue/lavagna/issues/13)
 - iCalendar feed support
 - update to spring 4.2.x
 - switch connection pool to hikaricp (simplify configuration)
 - use the browser locale for showing the correct first day of the week in the calendar (sunday or monday)

## 1.0.2 (2015-08-14)

 - Tomcat 7/8 fix as found in issue [#12](https://github.com/digitalfondue/lavagna/issues/12)

## 1.0.1 (2015-08-14)

 - IE related fix [1e4980e](https://github.com/digitalfondue/lavagna/commit/1e4980e9c3ef4a7a84dafe9a0088be361d90a1b1)
 - Update the search result after closing a card [1dfdb3e](https://github.com/digitalfondue/lavagna/commit/1dfdb3e5b02afad349b987099ac923038f7ed901)
 - Define the correct content types for the fonts [10d6e66](https://github.com/digitalfondue/lavagna/commit/10d6e66f9707093122d82b28ef54e6e87c85ae39)
 - Fix typo in doc [71b79be](https://github.com/digitalfondue/lavagna/commit/71b79bebafb62c0485a0870d96db0612d4297803)


## 1.0 (2015-08-05)

 - stable release
