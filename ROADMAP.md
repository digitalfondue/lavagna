# Roadmap

This is a high level roadmap. Lavagna has currently:

 - a stable 1.0.x branch where small fixes and features will be added. Some of the work done in the master branch will be backported.
 - the current master (1.1) where the big features are developed
 
 
## Expected for 1.0.4 (mid september - october 2015)

 - support gitlab oauth (gitlab.com AND other gitlab instances (self-hosted and not))
 
## Expected for 1.0.5 (october 2015)

 - support for internal password protected accounts

## Expected for 1.1 

 - decent i18n
 - improve the UI/UX
 - enable Content-Security-Policy
 - support commit log parsing (git, svn (?))
 - webhooks support
 - client side refactoring, porting to angular 1.4.x [wip]
 - support internal account handling [wip]
 - refactor the authentication manager: cleanup + simplification [mostly done]
 - support gitlab oauth [done]
 - iCalendar feed support [done]
 - update to spring 4.2.x [done]
 - switch connection pool to hikaricp [done]
 
