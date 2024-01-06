package edu.fudan.selab.llm.jtype.provider

import edu.fudan.selab.utils.CoursierUtils
import org.junit.Test

import scala.jdk.CollectionConverters._

class CoursierUtilsTest {
  @Test
  def downloadTest(): Unit = {
    val dep1 = CoursierUtils.makeDependency("org.scalaz:scalaz-core_2.11:7.2.3")
    val deps1 = CoursierUtils.downloadDepSources(List(
      "org.scalaz:scalaz-core_2.11:7.2.3",
      "io.get-coursier:coursier_2.13:2.1.5"
    ))
    val deps2 = CoursierUtils.downloadDepJars(List(
      "org.scalaz:scalaz-core_2.11:7.2.3",
      "io.get-coursier:coursier_2.13:2.1.5"
    ))
    assert(dep1 != null)
    assert(deps1.nonEmpty && deps2.nonEmpty)
    assert(deps1.size == deps2.size)

    // jdt has something wrong to download
    val deps3 = CoursierUtils.downloadDepSources(List(
      "org.eclipse:jdt:3.2.1-r321_v20060823"
    ))
    assert(deps3.isEmpty)
    val deps4 = CoursierUtils.downloadDepJars(List(
      "org.eclipse:jdt:3.2.1-r321_v20060823"
    ))
    assert(deps4.isEmpty)
  }

  @Test
  def resolveTest(): Unit = {
    val deps_str = List(
      "org.scalaz:scalaz-core_2.11:7.2.3",
      "io.get-coursier:coursier_2.13:2.1.5"
    )
    val deps1 = CoursierUtils.makeDependencies(deps_str)
    val resolution = CoursierUtils.resolveDependencies(deps_str)
    val downloaded = CoursierUtils.downloadDepJars(deps_str)
    assert(resolution.size == downloaded.size)

    // cannot resolve this dependency
    val deps2_str = List(
      "com.google.common:google-collect:1.0-rc1",
      "org.eclipse:jdt:3.2.1-r321_v20060823"
    )
    val deps2 = CoursierUtils.makeDependencies(deps2_str)
    val resolution2 = CoursierUtils.resolveDependencies(deps2_str)
    val downloaded2 = CoursierUtils.downloadDepJars(deps2_str)
    assert(resolution2.isEmpty && downloaded2.isEmpty)
  }

  @Test
  def downloadDependenciesTest(): Unit = {
    val deps1_str = List(
      "org.scalaz:scalaz-core_2.11:7.2.3",
      "io.get-coursier:coursier_2.13:2.1.5"
    )
    val res = CoursierUtils.downloadDependencies(deps1_str.asJava)
    assert(res != null)
  }
}
