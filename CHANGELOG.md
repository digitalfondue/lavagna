# Changelog

## 1.1.2 (2018-07-20)

 - Fix download export for Firefox (see https://github.com/digitalfondue/lavagna/issues/107)
 - Add the possibility to configure lavagna from properties/file (see https://github.com/digitalfondue/lavagna/issues/108)

## 1.1.1 (2018-06-17)

 - Update dependencies
 - Fix html email issue (see https://github.com/digitalfondue/lavagna/issues/103)
 - Fix card d&d with the role only defined at the project level (see https://github.com/digitalfondue/lavagna/issues/105)

## 1.1 (2018-04-09)

 - Cleanup documentation. Stable release.

## 1.1-M8 (2018-02-20)

 - Remove the HSTS filter (see https://github.com/digitalfondue/lavagna/issues/97#issuecomment-366937891), updated dependencies.

## 1.1-M7 (2017-08-16)

 - Fix the "email to card" feature, updated dependencies.

## 1.1-M6 (2017-07-03)

 - Fixes, updated dependencies, add a more complete "create new card" functionality
 
## 1.0.7.4 (2017-07-03)

 - Updated dependencies, fix for tomcat

## 1.1-M5 (2017-04-05)

 - Fixes, improvements in the "email to card" feature.

## 1.1-M4 (2017-04-03)

 - Fixes, UI/UX improvements and "email to card" feature.

## 1.0.7.3 (2017-03-06)

 - Fix check ldap functionality and print more diagnostic information in the ui

## 1.1-M3 (2017-02-20)

 - Update the pre-release. Fixes and updates.

## 1.1-M2 (2017-02-09)

 - Update the pre-release. Multiple fixes and improvements.

## 1.1-M1 (2016-10-06)

 - Update the pre-release: ui/ux fixes.

## 1.0.7.2 (2016-10-06)

 - fix java7 compatibility

## 1.1-M0 (2016-10-02)

 - first pre-release of the 1.1 version. The user interface and experience has been completely revisited

## 1.0.7.1 (2016-10-02)

 - fix clone card issue (duplicated comments)
 - update dependencies

## 1.0.7 (2016-08-28)

 - better performance when handling boards with many custom labels or more than 200 cards
 - export an entire project to Excel
 - reworked the milestones in order to improve their usefulness 
   - milestone's detail page
   - improved cards list with "Column" and "Assigned to" columns
   - export a milestone to Excel 

## 1.0.6.2 (2016-03-28)

 - fix unsubscribe mechanism in stomp-client wrapper

## 1.0.6.1 (2016-03-25)

 - clone card functionality
 - milestones can have a release date
 - misc. minor UI/UX improvements
 - improved performances
 - updated libraries

(note: the 1.0.6 release has been skipped due to an error in the dependencies).

## 1.0.5.2 (2016-01-12)

 - fix file upload issue when a user don't have a global READ role.
 - misc minor UI/UX improvements

## 1.0.5.1 (2015-11-16)

 - Fix XSS issue in the file upload section (require an authenticated user)

## 1.0.5 (2015-11-07)

 - UI/UX improvements
   - change due date color based on the current date
   - card label: on mouse hover, show a tooltip with the card name
   - email notification: now the user can choose to receive only changes made by other users

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
