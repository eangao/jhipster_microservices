export interface IBlog {
  id?: string;
  title?: string;
  author?: string;
  post?: string;
}

export class Blog implements IBlog {
  constructor(public id?: string, public title?: string, public author?: string, public post?: string) {}
}

export function getBlogIdentifier(blog: IBlog): string | undefined {
  return blog.id;
}
