package com.github.bod.reproijpluginreferencesearch

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.components.service
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil
import com.github.bod.reproijpluginreferencesearch.services.MyProjectService
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf.className
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@TestDataPath("\$CONTENT_ROOT/testData/referenceSearch")
@RunWith(JUnit4::class)
class MyPluginTest : LightJavaCodeInsightFixtureTestCase() {
    private val mavenLibraries: List<String> = listOf(
        "com.apollographql.apollo:apollo-runtime:2.5.14",
        "com.apollographql.apollo:apollo-coroutines-support:2.5.14",
        "com.apollographql.apollo:apollo-normalized-cache-jvm:2.5.14",
        "com.apollographql.apollo:apollo-normalized-cache-sqlite-jvm:2.5.14",
        "com.apollographql.apollo:apollo-http-cache-api:2.5.14",
    )

    private val projectDescriptor = object : DefaultLightProjectDescriptor() {
        override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
            for (library in mavenLibraries) {
                addFromMaven(model, library, true, DependencyScope.COMPILE)
            }
        }
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return projectDescriptor
    }



    @Test
    fun testReferenceSearch() {
        myFixture.copyFileToProject("referenceSearch.kt")

        val psiLookupClass = JavaPsiFacade.getInstance(project).findClass("com.apollographql.apollo.coroutines.CoroutinesExtensionsKt", GlobalSearchScope.allScope(project))
        assertNotNull(psiLookupClass)

        val jvmMethods = psiLookupClass!!.findMethodsByName("await", false)
        assertEquals(2, jvmMethods.size)

        var found = false
        for (jvmMethod in jvmMethods) {
            found = ReferencesSearch.search(jvmMethod, GlobalSearchScope.projectScope(project), false).findAll().isNotEmpty()
            if (found) break
        }
        assertTrue(found)
    }


    override fun getTestDataPath() = "src/test/testData/referenceSearch"
}
