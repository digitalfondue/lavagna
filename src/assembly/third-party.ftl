<#-- To render the third-party file.
 Available context :

 - dependencyMap a collection of Map.Entry with
   key are dependencies (as a MavenProject) (from the maven project)
   values are licenses of each dependency (array of string)

 - licenseMap a collection of Map.Entry with
   key are licenses of each dependency (array of string)
   values are all dependencies using this license
-->
<#function licenseProjectFormat licenses>
	<#assign result = ""/>
		<#list licenses as license>
        <#assign result = result + "     License: " + license.name + " ("+ (license.url!"no url defined") + ")\n"/>
    </#list>
	<#return result>
</#function>
<#function artifactFormat p>
    <#if p.name?index_of('Unnamed') &gt; -1>
        <#return p.artifactId + " (" + (p.url!"no url defined") +"); " + p.groupId + ":" + p.artifactId + ":" + p.version + "\n" + licenseProjectFormat(p.licenses)>
    <#else>
        <#return p.name + " ("  + (p.url!"no url defined")+ "); " + p.groupId + ":" + p.artifactId + ":" + p.version + "\n" + licenseProjectFormat(p.licenses)>
    </#if>
</#function>

This product includes/uses the following libraries:

<#list dependencyMap as e>
	<#assign project = e.getKey()/>
	<#assign licenses = e.getValue()/>
 - ${artifactFormat(project)}
</#list>
 - winsw: Windows service wrapper in less restrictive license (https://github.com/kohsuke/winsw);
     License: The MIT License (http://www.opensource.org/licenses/mit-license.php)
 
 - Font Awesome by Dave Gandy (http://fontawesome.io)
     License: (http://fontawesome.io/license/)
       - Font License: SIL OFL 1.1 (http://scripts.sil.org/OFL)
       - Code License: MIT License (http://opensource.org/licenses/mit-license.html)

 - Material Design Lite (http://www.getmdl.io/)
     License: The Apache Software License, Version 2.0 (https://raw.githubusercontent.com/google/material-design-lite/master/LICENSE)

 - AngularJS (http://angularjs.org)
     License: MIT (https://github.com/angular/angular.js/blob/master/LICENSE)
 
 - Angular Material Design (https://github.com/angular/material)
     License: MIT (https://github.com/angular/material/blob/master/LICENSE)
     
 - angular ui.router (https://github.com/angular-ui/ui-router)
     License: MIT (https://github.com/angular-ui/ui-router/blob/master/LICENSE)
      
 - angular-translate (http://github.com/PascalPrecht/angular-translate)
     License: MIT (https://github.com/angular-translate/angular-translate/blob/master/LICENSE)
      
 - angular-file-upload (https://github.com/danialfarid/ng-file-upload)
     License: MIT (https://github.com/danialfarid/ng-file-upload/blob/master/LICENSE)

 - Chart.js (http://chartjs.org/)
     License: MIT (https://github.com/nnnick/Chart.js/blob/master/LICENSE.md)

 - cal-heatmap (https://kamisama.github.io/cal-heatmap/)
     License: MIT (https://github.com/kamisama/cal-heatmap/blob/master/LICENCE-MIT)
          
 - d3js (http://d3js.org/)
     License: BSD-3-Clause (https://github.com/mbostock/d3/blob/master/LICENSE)

 - Highlight (https://highlightjs.org/)
     License: BSD-3-Clause (https://github.com/isagalaev/highlight.js/blob/master/LICENSE)
     
 - jQuery (https://jquery.org/)
     License: MIT (https://jquery.org/license/)

 - marked (https://github.com/chjj/marked)
     License: MIT (https://github.com/chjj/marked/blob/master/LICENSE)
     
 - moment.js (http://momentjs.com/)
     License: MIT (https://github.com/moment/moment/blob/develop/LICENSE)
     
 - SockJS client (http://sockjs.org)
     License: MIT (https://github.com/sockjs/sockjs-client/blob/master/LICENSE)
     
 - angular-sortable-view (https://github.com/kamilkp/angular-sortable-view)
     License: MIT (https://github.com/kamilkp/angular-sortable-view/blob/master/LICENSE)
 
 - Spectrum Colorpicker (https://github.com/bgrins/spectrum)
     License: MIT (https://github.com/bgrins/spectrum/blob/master/LICENSE)
 
 - Stomp Over WebSocket (https://github.com/jmesnil/stomp-websocket)
     License: The Apache Software License, Version 2.0 (https://github.com/jmesnil/stomp-websocket/blob/master/LICENSE.txt)
 