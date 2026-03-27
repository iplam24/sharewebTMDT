<template>
  <RouterView />
  <AppConsent />
</template>

<script setup>
import { RouterView } from 'vue-router';
import AppConsent from '@/components/AppConsent.vue';
</script>

<style>
#app {
  position: relative;
  min-height: 100vh;
}

body {
  margin: 0;
  font-family: sans-serif;
}
</style>