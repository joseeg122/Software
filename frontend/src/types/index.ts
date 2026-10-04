export type SourceStatus = 'DISPONIBLE' | 'SIN_HALLAZGOS' | 'HALLAZGO' | 'NO_REGISTRA' | 'ERROR' | 'NO_DISPONIBLE';
export type MatchType = 'NINGUNA' | 'NOMINAL' | 'CONFIRMADA';
export type Severity = 'ALTO' | 'MEDIO' | 'BAJO' | 'SIN_HALLAZGOS';
export type SourceCategory =
  | 'BANCO' | 'LISTA_VINCULANTE' | 'LISTA_RESTRICTIVA' | 'PEP' | 'ANTECEDENTES' | 'JUDICIAL' | 'TRANSITO' | 'GENERAL';

export interface User { token: string; username: string; fullName: string; role: string }
export interface Person {
  id: number; fullName: string; document: string; documentType: string; city: string; birthDate: string;
  occupation: string; overallStatus: string; lastCheckedAt: string | null; runNumber: number;
}
export interface Bank { id: number; slug: string; name: string }
export interface Account {
  id: number; personId: number; bank: Bank; maskedNumber: string; type: string; status: string; balance: number;
  openedAt: string;
}
export interface Transaction {
  id: number; accountId: number; txDate: string; description: string; type: 'CREDITO' | 'DEBITO'; channel: string;
  amount: number; balanceBefore: number; balanceAfter: number;
}
export interface Credit {
  id: number; bank: Bank; maskedNumber: string; product: string; type: string; initialAmount: number;
  balance: number; installment: number; rate: number; termMonths: number; status: string; daysPastDue: number;
  restructured: boolean; openedAt: string; closedAt: string | null;
}
export interface CreditPayment {
  id: number; creditId: number; installmentNo: number; dueDate: string; paidDate: string | null; amount: number;
  status: string; daysLate: number;
}
export interface CreditHistory { id: number; creditId: number; eventDate: string; eventType: string; description: string }
export interface CreditScore {
  score: number; maxScore: number; calculatedAt: string; paymentHistory: number; debtLevel: number;
  cardUtilization: number; creditAge: number; activeCredits: number; delinquency: number;
}
export interface Source {
  id: number; code: string; name: string; category: SourceCategory; city: string | null; bankId: number | null;
}
export interface SourceResult {
  id: number; source: Source; status: SourceStatus; matchType: MatchType; matches: number; summary: string;
  checkedAt: string;
}
export interface Sanction {
  id: number; source: Source; listType: string; matchedName: string; matchType: MatchType; program: string;
  listedAt: string; detail: string;
}
export interface PepRecord {
  id: number; source: Source; matchedName: string; position: string; entity: string; matchType: MatchType;
  fromDate: string; toDate: string | null; detail: string;
}
export interface BackgroundCheck {
  id: number; source: Source; recordType: string; reference: string; matchedName: string; matchType: MatchType;
  recordDate: string; status: string; detail: string;
}
export interface JudicialProcess {
  id: number; source: Source; processNumber: string; processType: string; court: string; city: string;
  plaintiff: string; defendant: string; status: string; lastAction: string; lastActionDate: string; filedAt: string;
  matchType: MatchType; legalCollection: boolean;
}
export interface Lawsuit {
  id: number; processId: number; lawsuitNumber: string; claimType: string; plaintiff: string; defendant: string;
  amount: number; status: string; filedAt: string;
}
export interface LegalMeasure {
  id: number; processId: number; measureType: string; asset: string; amount: number; status: string;
  orderedAt: string;
}
export interface TrafficRecord {
  id: number; source: Source; recordType: string; reference: string; city: string; recordDate: string;
  amount: number; status: string; matchType: MatchType;
}
export interface News {
  id: number; source: Source; title: string; publishedAt: string; outlet: string; category: string; level: string;
  status: string; matchType: MatchType;
}
export interface Alert {
  id: number; severity: Severity; title: string; description: string; category: string; createdAt: string;
  status: string;
}
export interface Comment { id: number; author: string; body: string; createdAt: string; updatedAt: string }
export interface TimelineEvent { id: number; eventType: string; description: string; occurredAt: string }
export interface Dashboard {
  personId: number; fullName: string; document: string; overallStatus: string; lastCheckedAt: string | null;
  banksConnected: number; accounts: number; totalDebt: number; activeCredits: number; processes: number;
  findings: number; score: number | null; maxScore: number | null; unavailableSources: number;
}
export interface Profile {
  person: Person; dashboard: Dashboard; sourceResults: SourceResult[]; unavailableSources: SourceResult[];
  accounts: Account[]; transactions: Transaction[]; credits: Credit[]; creditPayments: CreditPayment[];
  creditHistory: CreditHistory[]; score: CreditScore | null; sanctions: Sanction[]; pepRecords: PepRecord[];
  backgroundChecks: BackgroundCheck[]; judicialProcesses: JudicialProcess[]; lawsuits: Lawsuit[];
  legalMeasures: LegalMeasure[]; trafficRecords: TrafficRecord[]; news: News[]; alerts: Alert[];
  comments: Comment[]; timeline: TimelineEvent[]; disclaimer: string;
}
export interface Report {
  id: number; code: string; personId: number; generatedAt: string; generatedBy: string; checkStatus: string;
  snapshot: Profile;
}
export interface SearchResult { type: string; label: string; detail: string; personId: number | null; route: string }
export interface AiResponse {
  question: string; systemData: string[]; interpretation: string; engine: string; notice: string;
}
