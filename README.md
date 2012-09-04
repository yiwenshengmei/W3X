# W3X

## 项目介绍

* W3X是一个集抓取Web页面、解析Web页面和生成Web页面为一体的编程框架，主要用于将Web页面上的数据按指定解析规则抓取到本地，并按照指定的格式重新排版显示。适合网站的开发者在没有开发手持设备软件和相关服务端服务的情况下，仅通过编写解析脚本和制作模板就可以轻松完成Web页面到手持设备软件的转换。
* W3X框架分为一套抓取、解析框架和一套模板开发工具，开发者需要通过编写脚本来定义抓取对象的Web地址、抓取方式、保存形式等，框架会读取脚本并执行相关逻辑。模板定义了新布局的格式，在需要显示的时候W3X通过将模板与抓取后的数据结合生成新的页面输出在设备上。
* W3X在部署时只需将W3X本身和开发者编写的脚本及模板一起安装至用户的Android手持设备中即可。

## 作者

* 姓名：赵杰
* 微博：http://weibo.com/yiwenshengmei
*   QQ: 31665114

## 项目依赖

* JavaSE-1.6
* Junit 4
* commons-collections-3.1
* commons-codec-1.4
* commons-io.2.0.1
* commons-lang-2.5
* common-logging-1.1.1
* htmlcleaner-2.2
* httpclient-4.1.2
* httpclient-cache-4.1.2
* httpcore-4.1.2
* httpmime-4.1.2
* jsoup-1.6.3
* log4j-1.2.16
* slf4j-api.1.6.1
* slf4j-log4j12-1.6.1
* sllitejdbc-v056
