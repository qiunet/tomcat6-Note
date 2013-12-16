tomcat6-Note
============




为tomcat6翻译部分类的注释.以及实现的讲解


学习应该做到知其然. 并且知其所以然.


永不停止追求卓越的脚步!



tomcat 通过bootstrap main方法. 启动.  在启动的时候.根据main 的参数.设置了一些环境变量 .

反射调用Catalina. start().


整个tomcat有个结构的概念.

  Catalina -> Server -> Service ->   >容器/连接器/日志器
  
  在Catalina的createStartDigester() 通过解析conf/server.xml  达到上述关联.
  
  在Catalina的start()  .启动Server.  进而启动所有子组件.实现tomcat的运行.
  
  
  接受请求的顺序:
  jioEndPoint-> Http11Protocol -> StandardEngine -> StandardHost -> StandardContext-> StandardWrapper 
  
  ***** 代码没有删减. 没有改变原有英文注释. 注释并不一定代表正确. 如果有不一样的理解.可以留言. 一起探讨.
  																													
  																														--qiunet
	  																													2013年12月16日 
