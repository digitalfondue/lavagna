# Roadmap

This is a high level roadmap. Lavagna has currently:

 - a stable 1.0.x branch where small fixes and features will be added
 - the current master (1.1) where the big features are developed

## Expected for 1.0.3

 - iCalendar feed support
 - update to spring 4.2.x
 - switch connection pool to hikaricp (simplify configuration)
 - use the browser locale for showing the correct first day of the week in the calendar (sunday or monday)

## Expected for 1.1 

 - client side refactoring, porting to angular 1.4.x
 - improve the UI/UX
 - enable Content-Security-Policy
 - switch the authentication manager to spring-security or apache shiro
 - support gitlab oauth 
 - support internal account handling
 - support commit log parsing (git, svn (?))
 - webhooks support
 - update to spring 4.2.x [done]
 - switch connection pool to hikaricp [done]

  