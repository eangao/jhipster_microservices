import { ISession } from 'app/entities/session/session.model';

export interface ISpeaker {
  id?: number;
  firstName?: string;
  lastName?: string;
  email?: string;
  twitter?: string;
  bio?: string;
  sessions?: ISession[] | null;
}

export class Speaker implements ISpeaker {
  constructor(
    public id?: number,
    public firstName?: string,
    public lastName?: string,
    public email?: string,
    public twitter?: string,
    public bio?: string,
    public sessions?: ISession[] | null
  ) {}
}

export function getSpeakerIdentifier(speaker: ISpeaker): number | undefined {
  return speaker.id;
}
