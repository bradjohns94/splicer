name := "Splicer"
description := "Combine any number of people's twitters"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.github.scopt"    %%  "scopt"                 % "3.5.0",
  "com.typesafe"        %   "config"                % "1.3.1",
  "org.deeplearning4j"  %   "deeplearning4j-core"   % "0.7.2",
  "org.deeplearning4j"  %   "deeplearning4j-parent" % "0.7.2",
  "org.nd4j"            %   "nd4j"                  % "0.7.2",
  "org.nd4j"            %   "nd4j-native-platform"  % "0.7.2",
  "org.scala-lang" % "scala-compiler" % "2.12.1"
)
