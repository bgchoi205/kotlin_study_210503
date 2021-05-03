import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val articleRepository = ArticleRepository()
val memberRepository = MemberRepository()
val boardRepository = BoardRepository()

var loginedMember : Member? = null

fun main() {
    println("==게시물 프로그램 시작==")

    val articleController = ArticleController()
    val memberController = MemberController()
    val boardController = BoardController()

    articleRepository.makeTestArticles()
    memberRepository.makeTestMember()

    while (true) {

        val prompt = if(loginedMember == null){
            "명령어 입력 : "
        }else{
            "${loginedMember!!.nickName})"
        }
        print(prompt)
        val cmd = readLineTrim()

        val rq = Rq(cmd)

        when (rq.actionPath) {

            "/system/exit" -> {
                println("종료")
                break
            }
            "/article/write" -> {
                articleController.write()
            }
            "/article/list" -> {
                articleController.list(rq)
            }
            "/article/detail" -> {
                articleController.detail(rq)
            }
            "/article/delete" -> {
                articleController.delete(rq)
            }
            "/article/modify" -> {
                articleController.modify(rq)
            }
            "/member/join" -> {
                memberController.join()
            }
            "/member/login" -> {
                memberController.login()
            }
            "/member/logout" -> {
                memberController.logout()
            }
            "/board/list" -> {
                boardController.list()
            }
            "/board/add" -> {
                boardController.add()
            }

        }

    }

    println("==게시물 프로그램 끝==")
}


// Board 시작
// Board DTO
data class Board(
    val boardId : Int,
    val boardName : String,
    val boardCode : String,
    val regDate : String
)

// BoardRepository 시작
class BoardRepository{
    val boards = mutableListOf<Board>(
        Board(1, "공지사항", "notice", Util.getDateNowStr()),
        Board(1, "자유게시판", "free", Util.getDateNowStr())
    )


}

// BoardRepository 끝


// BoardController 시작
class BoardController{
    fun list(){
        for(board in boardRepository.boards){
            println("번호 : ${board.boardId} / 이름 : ${board.boardName} / 코드 : ${board.boardCode} / 등록일 : ${board.regDate}  ")
        }
    }

    fun add() {
        print("게시판 이름 입력 : ")
        val boardName = readLineTrim()
        print("게시판 코드 입력 : ")
        val boardCode = readLineTrim()
    }
}

// BoardController 끝

// Member 시작
// Member DTO
data class Member(
    val id : Int,
    val loginId : String,
    val loginPw : String,
    val name : String,
    val nickName : String
)

// MemberRepository 시작
class MemberRepository{
    val members = mutableListOf<Member>()
    var lastMemberId = 0

    fun joinMember(loginId: String, loginPw: String, name: String, nickName: String): Int {
        val id = ++lastMemberId
        members.add(Member(id, loginId, loginPw, name, nickName))
        return id
    }

    fun makeTestMember(){
        for(i in 1..20){
            joinMember("user$i","user$i","홍길동$i", "사용자$i")
        }
    }

    fun getMemberByLoginId(loginId: String): Member? {
        for(member in members){
            if(member.loginId == loginId){
                return member
            }
        }
        return null
    }

    fun memberLoginIdCheck(loginId: String): Boolean {
        val member = getMemberByLoginId(loginId)
        return member == null
    }

    fun getMemberById(memberId: Int): Member? {
        for(member in members){
            if(member.id == memberId){
                return member
            }
        }
        return null
    }
}

// MemberRepository 끝


// MemberController 시작
class MemberController{
    fun join() {
        print("사용할 아이디 입력 : ")
        val loginId = readLineTrim()
        val loginIdCheck = memberRepository.memberLoginIdCheck(loginId)
        if(loginIdCheck == false){
            println("사용중인 아이디입니다.")
            return
        }
        print("사용할 비밀번호 입력 : ")
        val loginPw = readLineTrim()
        print("이름 입력 : ")
        val name = readLineTrim()
        print("별명 입력 : ")
        val nickName = readLineTrim()
        val id = memberRepository.joinMember(loginId, loginPw, name, nickName)

        println("$id 번 회원으로 가입 완료")
    }

    fun login() {
        print("아이디 입력 : ")
        val loginId = readLineTrim()
        val member = memberRepository.getMemberByLoginId(loginId)
        print("비밀번호 입력 : ")
        val loginPw = readLineTrim()
        if(member!!.loginPw != loginPw){
            println("비밀번호가 틀립니다.")
            return
        }
        loginedMember = member
        println("${member.nickName}님 환영합니다.")
    }

    fun logout() {
        loginedMember = null
        println("로그아웃")
    }


}

// MemberController 끝


// Member 끝



// Article 시작
// Article DTO
data class Article(
    val id: Int,
    var title: String,
    var body: String,
    val memberId : Int,
    val regDate: String,
    var updateDate: String
)

// ArticleRepository 시작
class ArticleRepository {

    val articles = mutableListOf<Article>()
    var lastArticleId = 0

    fun addArticle(title : String, body : String, memberId : Int) : Int{
        val id = ++lastArticleId
        val regDate = Util.getDateNowStr()
        val updateDate = Util.getDateNowStr()
        articles.add(Article(id, title, body, memberId, regDate, updateDate))
        return id
    }

    fun makeTestArticles(){
        for(i in 1..30){
            addArticle("제목$i", "내용$i", i % 9 + 1)
        }
    }

    fun getArticleById(id: Int): Article? {
        for(article in articles){
            if(article.id == id){
                return article
            }
        }
        return null
    }

    fun articlesFilter(keyword: String, page: Int, pageCount: Int): List<Article>? {
        val filtered1Articles = articlesFilterByKey(keyword)
        val filtered2Articles = mutableListOf<Article>()
        if(filtered1Articles.isEmpty()){
            return null
        }
        val startIndex = filtered1Articles.lastIndex - ((page - 1) * pageCount)
        var endIndex = startIndex - pageCount + 1
        if(endIndex < 0){
            endIndex = 0
        }
        for(i in startIndex downTo endIndex){
            filtered2Articles.add(filtered1Articles[i])
        }
        return filtered2Articles
    }

    private fun articlesFilterByKey(keyword: String): List<Article> {
        val filtered1Articles = mutableListOf<Article>()
        for(article in articles){
            if(article.title.contains(keyword)){
                filtered1Articles.add(article)
            }
        }
        return filtered1Articles
    }

}

// ArticleRepository 끝


// ArticleController 시작
class ArticleController {
    fun write() {
        if(loginedMember == null){
            println("로그인 후 이용해주세요")
            return
        }
        print("제목입력 : ")
        val title = readLineTrim()
        print("내용입력 : ")
        val body = readLineTrim()
        val memberId = loginedMember!!.id

        val id = articleRepository.addArticle(title, body, memberId)
        println("$id 번 게시물 등록 완료")
    }

    fun list(rq : Rq) {
        if(loginedMember == null){
            println("로그인 후 이용해주세요")
            return
        }
        var keyword = rq.getStringParam("keyword","")
        var page = rq.getIntParam("page", 1)

        val articles = articleRepository.articlesFilter(keyword, page, 5)
        if(articles == null){
            println("검색된 게시물이 없습니다.")
            return
        }
        for(article in articles){
            val member = memberRepository.getMemberById(article.memberId)
            val nickName = member!!.nickName
            println("번호 : ${article.id} / 등록일 : ${article.regDate} / 제목 : ${article.title} / 작성자 : ${nickName}")
        }

    }

    fun detail(rq: Rq) {
        if(loginedMember == null){
            println("로그인 후 이용해주세요")
            return
        }
        val id = rq.getIntParam("id", 0)
        if(id == 0){
            println("게시물 번호를 입력해주세요")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        println("번호 : ${article.id}")
        println("제목 : ${article.title}")
        println("내용 : ${article.body}")
        println("등록일 : ${article.regDate}")
        println("수정일 : ${article.updateDate}")
    }

    fun delete(rq: Rq) {
        if(loginedMember == null){
            println("로그인 후 이용해주세요")
            return
        }
        val id = rq.getIntParam("id", 0)
        if(id == 0){
            println("게시물 번호를 입력해주세요")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        if(loginedMember!!.id != article.memberId){
            println("권한이 없습니다.")
            return
        }
        articleRepository.articles.remove(article)
        println("$id 번 게시물 삭제 완료")
    }

    fun modify(rq: Rq) {
        if(loginedMember == null){
            println("로그인 후 이용해주세요")
            return
        }
        val id = rq.getIntParam("id", 0)
        if(id == 0){
            println("게시물 번호를 입력해주세요")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        if(loginedMember!!.id != article.memberId){
            println("권한이 없습니다.")
            return
        }
        print("새 제목 : ")
        val title = readLineTrim()
        print("새 내용 : ")
        val body = readLineTrim()
        val updateDate = Util.getDateNowStr()
        article.title = title
        article.body = body
        article.updateDate = updateDate
        println("$id 번 게시물 수정 완료")
    }
}

// ArticleController 끝

// Article 끝


fun readLineTrim() = readLine()!!.trim()

object Util {
    fun getDateNowStr(): String {
        var now = LocalDateTime.now()
        var getNowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분 ss초"))
        return getNowStr
    }
}