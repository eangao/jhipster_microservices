import * as dayjs from 'dayjs';
import { ISpeaker } from 'app/entities/speaker/speaker.model';

export interface ISession {
  id?: number;
  title?: string;
  description?: string;
  startDateTime?: dayjs.Dayjs;
  endDateTime?: dayjs.Dayjs;
  speakers?: ISpeaker[] | null;
}

export class Session implements ISession {
  constructor(
    public id?: number,
    public title?: string,
    public description?: string,
    public startDateTime?: dayjs.Dayjs,
    public endDateTime?: dayjs.Dayjs,
    public speakers?: ISpeaker[] | null
  ) {}
}

export function getSessionIdentifier(session: ISession): number | undefined {
  return session.id;
}
