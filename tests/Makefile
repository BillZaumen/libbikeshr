
CPATH = classes:../BUILD/libbikeshr.jar:/usr/share/java/libbzdev.jar

JAVA = java -classpath $(CPATH)
JAVAC = javac -d classes -classpath $(CPATH)

all: test1 test2 test3 test4 test5 test6 test7

test2a: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2a.svg -vS:gtype:svg test2a.js

test2a-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:tmp.png  test2a.esp

descr-yaml:
	yrunner -p  ../BUILD/libbikeshr.jar \
		--link /usr/share/doc/libbzdev-doc/api/ \
		--link /usr/share/doc/openjdk-11-doc/api/ \
		--link ../BUILD/api/ --generateDocs \
		drama,bikeshr,graphics,rv > descr-yaml.html

test2a-yaml: lib
	yrunner -r -p  ../BUILD/libbikeshr.jar \
		--stackTrace \
		-o:gout:tmp.png -vS:gtype:png \
		drama,bikeshr,graphics,rv test2a.yaml

test2b-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2b.svg -vS:gtype:svg test2b.js

test2b-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2b.svg -vS:gtype:svg test2b.esp

test2c-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2c.svg -vS:gtype:svg test2c.js

test2c-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2c.svg -vS:gtype:svg test2c.esp

test2d-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2d.svg -vS:gtype:svg test2d.js

test2d-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2d.svg -vS:gtype:svg test2d.esp

test2e-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-d:da:`pwd` -vS:filename:test2e.svg test2e.js
test2e-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-d:da:`pwd` -vS:filename:test2e.svg test2e.esp

test2f-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2f.svg -vS:gtype:svg test2f.js

test2f-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2f.svg -vS:gtype:svg test2f.esp


test2aPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2a.ps -vS:gtype:ps test2a.js
	ps2eps -P test2a.ps
	rm test2a.ps

test2aPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2a.ps -vS:gtype:ps test2a.esp
	ps2eps -P test2a.ps
	rm test2a.ps

test2bPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2b.ps -vS:gtype:ps test2b.js
	ps2eps -P test2b.ps
	rm test2b.ps

test2bPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2b.ps -vS:gtype:ps test2b.esp
	ps2eps -P test2b.ps
	rm test2b.ps


test2cPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2c.ps -vS:gtype:ps test2c.js
	ps2eps -P test2c.ps
	rm test2c.ps

test2cPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2c.ps -vS:gtype:ps test2c.esp
	ps2eps -P test2c.ps
	rm test2c.ps


test2dPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2d.ps -vS:gtype:ps test2d.js
	ps2eps -P test2d.ps
	rm test2d.ps

test2dPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2d.ps -vS:gtype:ps test2d.esp
	ps2eps -P test2d.ps
	rm test2d.ps

test2ePS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-d:da:`pwd` -vS:filename:test2e.ps test2e.js
	ps2eps -P test2e.ps
	rm test2e.ps

test2ePS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-d:da:`pwd` -vS:filename:test2e.ps test2e.esp
	ps2eps -P test2e.ps
	rm test2e.ps


test2fPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2f.ps -vS:gtype:ps test2f.js
	ps2eps -P test2f.ps
	rm test2f.ps

test2fPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test2f.ps -vS:gtype:ps test2f.esp
	ps2eps -P test2f.ps
	rm test2f.ps

test3a-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test3a.svg -vS:gtype:svg --exit test3a.js

test3a-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test3a.svg -vS:gtype:svg --exit test3a.esp

test3b-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar --trustLevel=2 \
		-o:gout:test3b.svg -vS:gtype:svg  --exit \
		test3b.js

test3b-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar  --trustLevel=2 \
		-o:gout:test3b.svg -vS:gtype:svg  --exit \
		test3b.esp


test3aPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test3a.ps -vS:gtype:ps test3a.js
	rm -f test3a.eps
	ps2eps -P test3a.ps
	rm test3a.ps

test3aPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test3a.ps -vS:gtype:ps test3a.esp
	rm -f test3a.eps
	ps2eps -P test3a.ps
	rm test3a.ps


test3bPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar  --trustLevel=2 \
		-o:gout:test3b.ps -vS:gtype:ps  --exit\
		test3b.js
	rm -f test3b.eps
	ps2eps -P test3b.ps
	rm test3b.ps

test3bPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar  --exit  --trustLevel=2 \
		-o:gout:test3b.ps -vS:gtype:ps test3b.esp
	rm -f test3b.eps
	ps2eps -P test3b.ps
	rm test3b.ps


test3f-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar --exit \
		-o:gout:test3f.svg -vS:gtype:svg test3f.js

test3f-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar --exit \
		-o:gout:test3f.svg -vS:gtype:svg test3f.esp


test3fPS: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test3f.ps -vS:gtype:ps test3f.js
	rm -f test3f.eps
	ps2eps -P test3f.ps
	rm test3f.ps


classes: lib
	mkdir -p classes

lib:
	(cd ..; make)

test1: classes
	$(JAVAC) Test1.java
	$(JAVA) Test1

test2: classes
	$(JAVAC) Test2.java
	$(JAVA) Test2

test3: classes
	$(JAVAC) Test3.java
	$(JAVA) Test3

test4: classes
	$(JAVAC) Test4.java
	$(JAVA) Test4

test5: classes
	$(JAVAC) Test5.java
	$(JAVA) Test5

test6: classes
	$(JAVAC) Test6.java
	$(JAVA) Test6

test7: classes
	$(JAVAC) Test7.java
	$(JAVA) Test7

test8: classes
	$(JAVAC) Test8.java
	$(JAVA) Test8


test1p: classes
	$(JAVAC) Test1.java
	$(JAVA) Test1 printOnly

test2js: classes
	scrunner -p ../BUILD/libbikeshr.jar --stackTrace test2.js

test20a-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20a.svg -vS:gtype:svg test20a.js

test20a-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20a.svg -vS:gtype:svg test20a.esp


test20b-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20b.svg -vS:gtype:svg test20b.js

test20b-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20b.svg -vS:gtype:svg test20b.esp


test20aPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20a.ps -vS:gtype:ps test20a.js
	rm -f test20a.eps
	ps2eps -P test20a.ps
	rm test20a.ps

test20aPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20a.ps -vS:gtype:ps test20a.esp
	rm -f test20a.eps
	ps2eps -P test20a.ps
	rm test20a.ps

test20bPS-js: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20b.ps -vS:gtype:ps test20b.js
	rm -f test20b.eps
	ps2eps -P test20b.ps
	rm test20b.ps

test20bPS-esp: lib
	scrunner -r -p ../BUILD/libbikeshr.jar \
		-o:gout:test20b.ps -vS:gtype:ps test20b.esp
	rm -f test20b.eps
	ps2eps -P test20b.ps
	rm test20b.ps

clean:
	rm -rf classes
	rm -f tmp.png
