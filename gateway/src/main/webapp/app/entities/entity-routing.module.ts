import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'speaker',
        data: { pageTitle: 'Speakers' },
        loadChildren: () => import('./speaker/speaker.module').then(m => m.SpeakerModule),
      },
      {
        path: 'session',
        data: { pageTitle: 'Sessions' },
        loadChildren: () => import('./session/session.module').then(m => m.SessionModule),
      },
      {
        path: 'blog',
        data: { pageTitle: 'Blogs' },
        loadChildren: () => import('./blog/blog/blog.module').then(m => m.BlogBlogModule),
      },
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ]),
  ],
})
export class EntityRoutingModule {}
