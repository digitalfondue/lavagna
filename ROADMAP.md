# Roadmap

This is a high level roadmap. Lavagna has currently:

 - a stable 1.0.x branch where small fixes and features will be added. Some of the work done in the master branch will be backported.
 - the current master (1.1) where the big features are developed

## Expected for 1.0.3 (~september 2015)

 - fix issue [#13](https://github.com/digitalfondue/lavagna/issues/13)
 - iCalendar feed support [done]
 - update to spring 4.2.x  [done]
 - switch connection pool to hikaricp (simplify configuration)  [done]
 - use the browser locale for showing the correct first day of the week in the calendar (sunday or monday)  [done]

## Expected for 1.1 

 - client side refactoring, porting to angular 1.4.x
 - decent i18n
 - improve the UI/UX
 - enable Content-Security-Policy
 - support gitlab oauth 
 - support internal account handling
 - support commit log parsing (git, svn (?))
 - webhooks support
 - refactor the authentication manager: cleanup + simplification [WIP]
 - iCalendar feed support [done]
 - update to spring 4.2.x [done]
 - switch connection pool to hikaricp [done]

  
