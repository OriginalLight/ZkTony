import { createRouter, createWebHashHistory } from "vue-router";
import Index from "./components/Index.vue";
import Serial from "./components/Serial.vue";
import Procotol from "./components/Protocol.vue";

const routes = [
  {
    path: "/",
    component: Index,
  },
  {
    path: "/serial",
    component: Serial,
  },
  {
    path: "/protocol",
    component: Procotol,
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

export default router;
