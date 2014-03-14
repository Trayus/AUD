AUD
===

Home of AUD.js and AUD.java (master's thesis work)

AUD is a system for procedurally generating music based off of a mood. This mood is given by 2 floats: stress & energy (the Thayer model of music mood). AUD is capable of adapting its generated music on the fly, making it an excellent tool for games. Whenever a shift in mood happens in the game, the music can be adapted with a single function call to reflect that change. 

To integrate AUD into a javascript application, all you need to do is include the aud.js file (but currently it can only be run on Chrome due to dependency on WebkitAudio). To integrate the java version into a java application, include all .java files (except AUD_tester.java) and create a new AUD object. After that, you'll be able to control the audio with the relatively simple API, which is documented at <a href="timotheyadam.com/AUD" />.
