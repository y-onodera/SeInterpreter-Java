<#compress>
<#assign 
  resourceCDN=config.resourceCDN
  cdnURI="cdn.jsdelivr.net/gh/extent-framework/extent-github-cdn@" 
  csscommit="d6562a79075e061305ccfdb82f01e5e195e2d307"
  jscommit="d6562a79075e061305ccfdb82f01e5e195e2d307" 
  iconcommit="b00a2d0486596e73dd7326beacf352c639623a0e">
<#if resourceCDN=="extentreports">
  <#assign 
    cdnURI="extentreports.com/resx" 
    csscommit="" 
    jscommit="" 
    iconcommit="">
</#if>

<head>
  <meta charset="<#if config.encoding??>${config.encoding}<#else>utf-8</#if>">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <title>${config.documentTitle}</title>
  <#if offline>
    <link rel="apple-touch-icon" href="spark/logo.png">
    <link rel="shortcut icon" href="spark/logo.png">
    <link rel="stylesheet" href="spark/spark-style.css">
    <link rel="stylesheet" href="spark/font-awesome.min.css">
    <script src="spark/jsontree.js"></script>
  <#else>
    <link rel="apple-touch-icon" href="https://${cdnURI}${iconcommit}/commons/img/logo.png">
    <link rel="shortcut icon" href="https://${cdnURI}${iconcommit}/commons/img/logo.png">
    <link href="https://${cdnURI}${csscommit}/spark/css/spark-style.css" rel="stylesheet" />
    <link href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
    <script src="https://${cdnURI}7cc78ce/spark/js/jsontree.js"></script>
  </#if>
  <#include "../../commons/commons-inject-css.ftl">
</head>
</#compress>