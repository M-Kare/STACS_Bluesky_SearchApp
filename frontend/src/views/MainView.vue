<template>
  <Loading v-if="loading"/>
  <div class="container">
    <div class="leftside">
      <div>
        <h1>Welcome to BS-Search Stuff Thingy</h1>

        <input type="text" v-model="searchValue" />
        <input type="button" value="ok" @click="postSearch()"/>
        <input name="until" type="date" v-model="searchUntilDate"/>
        <select name="lang" v-model="searchLang">
          <option value="">None</option>
          <option value="nl">Dutch</option>
          <option value="en">English</option>
          <option value="fi">Finnish</option>
          <option value="fr">French</option>
          <option value="de">German</option>
          <option value="id">Indonesian</option>
          <option value="it">Italian</option>
          <option value="pt">Portuguese</option>
          <option value="es">Spanish</option>
          <option value="sw">Swedish</option>
        </select>
        <input type="button" value="X" @click="clearSearch()"/>
        <br>
        <label for="expand">expand</label>
        <input type="checkbox" name="expand" v-model="expandedSearch"/>
      </div>

      <div>
        <Pie class="pie" :data="chartData"/>
      </div>

      <PostPost class="postPost"/>

      <div class="summarize-similar-button-container">
        <button @click="summarizePosts">Summarize Posts</button>
        <button @click="findSimilarPosts">Find similar posts</button>
      </div>

      <div class="summary-box" v-if="textValue">
        <p class="summary">{{ textValue }}</p>
      </div>

    </div>

    <div class="postlist">
      <input type="radio"  id="allPosts" name="posts" value="all" checked v-model="allPosts">
      <label for="allPosts">All Posts</label>
      <input type="radio"  id="myPosts" name="posts" value="my" v-model="allPosts">
      <label for="myPosts">My Posts</label>
      <span id="postCounter">{{ sortedPosts.length }}</span>
      <PostListView v-if="sortedPosts" :posts="sortedPosts"/>
    </div>

  </div>
</template>

<script setup lang="ts">
import { Pie } from 'vue-chartjs'
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
} from 'chart.js'
import type {PostDTO} from "@/components/PostDTO.ts";
import {computed, ref, watchEffect} from "vue";
import PostListView from "@/views/PostListView.vue";
import PostPost from "@/components/PostPost.vue";
import Loading from "@/components/Loading.vue";
import type {GeminiDTO} from "@/components/GeminiDTO.ts";

ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale, LinearScale)
const urlRegex = /[/?&=#%+]+/
const posts = ref<Array<PostDTO>>([])
const sortedPosts = computed( () => {
  return posts.value.filter((post:PostDTO)=>post.content.toLocaleLowerCase().includes(searchValue.value.toString().toLocaleLowerCase()))
    .filter((post:PostDTO)=>searchLang.value.length > 1 ? post.lang?.includes(searchLang.value) : true)
    .filter((post:PostDTO)=>searchUntilDate.value.length > 1 ? post.timestamp <= searchUntilDate.value+"T23:59:59" : true)
    .sort((post1: PostDTO, post2: PostDTO)=>{
      return new Date(post2.timestamp).getTime() - new Date(post1.timestamp).getTime()
    })
})
const veryPositivePosts = ref<Array<PostDTO>>([])
const positivePosts = ref<Array<PostDTO>>([])
const neutralPosts = ref<Array<PostDTO>>([])
const negativePosts = ref<Array<PostDTO>>([])
const veryNegativePosts = ref<Array<PostDTO>>([])
const chartDataRef = computed( () => { return [veryPositivePosts.value.length,
                                                positivePosts.value.length,
                                                neutralPosts.value.length,
                                                negativePosts.value.length,
                                                veryNegativePosts.value.length]})
const chartData = computed( () => {
  return {
    "labels": [
      'Very Positive',
      'Positive',
      'Neutral',
      'Negative',
      'Very Negative'
    ],
    "datasets": [{
      data: chartDataRef.value,
      backgroundColor: [
        '#31cc5a',
        '#39854d',
        '#edcd58',
        '#cc5e62',
        '#c4181d'
      ]
    }]
  }
})

const textValue = ref("")

const searchLang = ref<string>("")
const searchUntilDate = ref<string>("")
const searchValue = ref<string>("")
const expandedSearch = ref<boolean>(false)
const allPosts = ref<string>("all")

const loading = ref<boolean>(false)

watchEffect( () => {
  veryPositivePosts.value = sortedPosts.value.filter((post:PostDTO)=>post.sentimentscore >= 3 && post.sentimentscore <= 4)
  positivePosts.value = sortedPosts.value.filter((post:PostDTO)=>post.sentimentscore > 2 && post.sentimentscore < 3)
  neutralPosts.value = sortedPosts.value.filter((post:PostDTO)=>post.sentimentscore == 2)
  negativePosts.value = sortedPosts.value.filter((post:PostDTO)=>post.sentimentscore < 2 && post.sentimentscore > 1)
  veryNegativePosts.value = sortedPosts.value.filter((post:PostDTO)=>post.sentimentscore <= 1 && post.sentimentscore >= 0)
})


/**
 * retrieves a limited number of posts from the database
 * limit is set in DatabaseHandler.kt
 */
async function fetchAllPosts() {
  loading.value = true
  const response = await fetch(`/api/posts/limited`)

  if (!response.ok) {
    loading.value = false
    throw new Error(`Error retrieving Posts from search: ${searchValue.value}. ${response.status}`)
  }

  const postData: Array<PostDTO> = await response.json()
  posts.value = postData
  loading.value =false
}
fetchAllPosts()

watchEffect(()=>{
  if(allPosts.value == "my"){
    fetchMyPosts()
  } else {
    fetchAllPosts()
  }
})

/**
 * fetches & displays your own posts from BlueSky
 */
async function fetchMyPosts() {
    loading.value = true
    const response = await fetch(`/api/posts/own`)
    if(!response.ok){
      loading.value = false
      throw new Error(`Error retrieving your Posts`)
    }
    const postData: Array<PostDTO> = await response.json()
    posts.value = postData
    loading.value = false
}

/**
 * calls the BlueSkyAPI to fetch posts matching the search criteria
 *
 * @param keywords - list of keywords, of which posts should be fetched
 */
async function postSearchKeywords(keywords: string[] = [searchValue.value]) {
  loading.value = true

  try {
    // Promise.all for parallel fetches
    const responses = await Promise.all(
      keywords.map(keyword =>
        fetch(`/api/search?q=${keyword}${searchUntilDate.value.length > 1 ? "&until=" + searchUntilDate.value + "T23:59:59Z" : ""}${searchLang.value.length > 1 ? "&lang=" + searchLang.value : ""}`)
      )
    )

    // check all answers and get data
    const allPosts: Array<PostDTO> = (await Promise.all(
      responses.map(async response => {
        if (!response.ok) {
          throw new Error(`Error retrieving posts. HTTP ${response.status}`)
        }

        return response.json()
      })
    )).flat() // combines nested arrays from Promise.all into 1 array of PostDTO, so it is iterable

    /**
     * creates map (no duplicates) from all posts and therefore returns only unique posts
     * this is needed for posts containing more than 1 matching keyword
     * otherwise the post would be saved multiple times
     */
    const uniquePosts = Array.from(
      new Map(
        allPosts.map(post => [post.uri, post])
      ).values()
    );

    posts.value = uniquePosts

  } catch (error) {
    console.error("Error in postSearch:", error)
  }

  loading.value = false
}

/**
 * calls the BlueSkyAPI to fetch posts matching the search criteria
 */
async function postSearch() {
  if (!searchValue.value.trim()) {
    alert("Please no empty")
    return
  }
  if (urlRegex.test(searchValue.value)) {
    alert("No funny business!")
    return
  }
  if(expandedSearch.value){
    return findTopic()
  }
  allPosts.value = "all"
  loading.value = true
  const response = await fetch(`/api/search?q=${searchValue.value}${searchUntilDate.value.length > 1 ? "&until="+searchUntilDate.value+"T23:59:59Z" : ""}${searchLang.value.length > 1 ? "&lang="+searchLang.value : ""}`)

  if(!response.ok) {
    loading.value = false
    const errorMessage = await response.text()
    alert(errorMessage)
    throw new Error(`Error retrieving Posts from search: ${searchValue.value}. ${response.status}`)
  }

  const postData: Array<PostDTO> = await response.json()
  posts.value = postData
  loading.value = false
}


/**
 * combines and formats the content of sortedPosts
 * the formatted contents are then send to gemini to get a summary of all posts
 */
async function summarizePosts() {
  const combinedPosts = sortedPosts.value.map((post: PostDTO) => post.content).join()
  const postsWithoutQuoteStrings = combinedPosts.replace(/"/g, "''")

  try {
    const response = await fetch("/api/gemini/summarize", {
      method: "POST",
      body: postsWithoutQuoteStrings
    })

    if (response.ok) {
        const responseData: GeminiDTO = await response.json();
        textValue.value = responseData.text;
      } else {
        textValue.value = `Error summarizing posts: HTTP ${response.status}`
      }
  } catch (error: any) {
    alert(`Failed to summarize posts - there are no posts for this keyword.\nError: ${error.value}`)
  }
}

/**
 * basically same functionality as findSimilarPosts, but the core implementation is written in BlueSkyHandler.kt -> expandedSearchPosts()
 */
async function findTopic() {
  loading.value = true
  const response = await fetch(`/api/search/expanded?q=${searchValue.value}${searchUntilDate.value.length > 1 ? "&until="+searchUntilDate.value+"T23:59:59Z" : ""}${searchLang.value.length > 1 ? "&lang="+searchLang.value : ""}`)
  if(!response.ok){
    loading.value = false
    throw new Error(`Error retrieving Posts from search: ${searchValue.value}. ${response.status}`)
  }
  const responseData: Array<PostDTO> = await response.json()
  posts.value = responseData
  loading.value = false
  searchValue.value = ""
}

/**
 * sends the keyword to gemini to get some similar words
 * calls postSearch() with these keywords to get more posts
 */
async function findSimilarPosts() {
  try {
    const response = await fetch("/api/gemini/findSimilarPosts", {
      method: "POST",
      body: searchValue.value
    })

    if (response.ok) {
        const responseData: GeminiDTO = await response.json()
        let geminiResponse: string = responseData.text

        textValue.value = `Similar posts containing ${geminiResponse} are now displayed`

        const keywords: string[] = geminiResponse.split(",")
        postSearchKeywords(keywords)
        searchValue.value = ""
      } else {
        textValue.value = `Error retrieving similar posts: HTTP ${response.status}`
      }
  } catch (error: any) {
    alert(`Failed to retrieve similar posts - error: ${error}`)
  }
}

/**
 * clears the search bar and fetches posts from the database
 */
function clearSearch() {
  searchLang.value = ""
  searchUntilDate.value = ""
  searchValue.value = ""
  allPosts.value = "all"
  fetchAllPosts()
}

</script>

<style scoped>
.container {
  display: flex;
  height: 100vh;
}

.pie{
  max-width: 400px;
  max-height: 400px;
}

#postCounter{
  position: absolute;
  right: 2rem;
}

.leftside {
  width: 50%;
  padding: 20px;
  overflow-y: auto;
}

.postlist {
  position: relative;
  width: 50%;
  padding: 20px;
  overflow-y: auto;
}

.postPost {
  width: 70%;
  border-radius: 8px;
}

.summarize-similar-button-container {
  margin-top: 2rem;
  display: flex;
  gap: 2rem;
}

.summary-box {
  background-color: white;
  padding: 2px 10px;
  max-height: 200px;
  overflow-y: auto;
  margin-top: 20px;
}

.summary {
  color: black;
}
</style>
