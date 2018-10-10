package com.artaeum.media.controller

import java.nio.file.{Files, Paths}
import java.util.Collections

import org.junit.{After, Before, Test}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.{MockHttpServletRequestBuilder, MockMvcRequestBuilders}
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{get, delete}
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{content, status}
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@RunWith(classOf[SpringRunner])
@SpringBootTest
class ImageControllerTest {

  val TEST_RESOURCE = "testResource"

  val TEST_NAME_WITHOUT_EXPANSION = "uuid-avatar"

  val TEST_NAME: String = TEST_NAME_WITHOUT_EXPANSION + ".jpg"

  @Value("classpath:image.png")
  var imagePNG: Resource = _

  @Autowired
  var imageController: ImageController = _

  var mockMvc: MockMvc = _

  @Before
  def init(): Unit = this.mockMvc = MockMvcBuilders.standaloneSetup(this.imageController).build

  @Test
  def whenGetNotExistingImage(): Unit = {
    this.mockMvc
      .perform(get("/images/{resource}/testtest.jpg", TEST_RESOURCE))
      .andExpect(status.isNotFound)
  }

  @Test
  def whenUploadImageAndGetIt(): Unit = {
    val file = new MockMultipartFile("image", "testimage.png",
      "image/png", Files.readAllBytes(Paths.get(this.imagePNG.getURI)))
    val builder = MockMvcRequestBuilders.multipart("/images/{resource}", TEST_RESOURCE)
      .file(file).param("imageName", TEST_NAME_WITHOUT_EXPANSION)
      .principal(new UsernamePasswordAuthenticationToken(
        "testuser", "password", Collections.emptyList[SimpleGrantedAuthority]))
    this.mockMvc.perform(builder).andExpect(status.isOk)
    this.mockMvc.perform(get("/images/{resource}/{name}", TEST_RESOURCE, TEST_NAME))
      .andExpect(status.isOk)
      .andExpect(content.contentType(MediaType.IMAGE_JPEG))
  }

  @Test
  def whenUploadImageAndDeleteItAndGetIt(): Unit = {
    val file = new MockMultipartFile("image", "testimage.png",
      "image/png", Files.readAllBytes(Paths.get(this.imagePNG.getURI)))
    val builder = MockMvcRequestBuilders.multipart("/images/{resource}", TEST_RESOURCE)
      .file(file).param("imageName", TEST_NAME_WITHOUT_EXPANSION)
      .principal(new UsernamePasswordAuthenticationToken(
        "testuser", "password", Collections.emptyList[SimpleGrantedAuthority]))
    this.mockMvc.perform(builder).andExpect(status.isOk)
    this.mockMvc.perform(delete("/images/{resource}/{name}", TEST_RESOURCE, TEST_NAME)
      .principal(new UsernamePasswordAuthenticationToken(
        "testuser", "password", Collections.emptyList[SimpleGrantedAuthority])))
    this.mockMvc.perform(get("/images/{resource}/{name}", TEST_RESOURCE, TEST_NAME))
      .andExpect(status.isNotFound)
  }

  @After
  def clear(): Unit = {
    val path = Paths.get(TEST_RESOURCE, TEST_NAME)
    if (Files.exists(path)) Files.delete(path)
  }
}